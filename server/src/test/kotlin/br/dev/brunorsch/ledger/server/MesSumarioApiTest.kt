package br.dev.brunorsch.ledger.server

import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MesSumarioApiTest : SuportePostgres() {

	@Autowired
	lateinit var mockMvc: MockMvc

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	@Autowired
	lateinit var objectMapper: JsonMapper

	companion object {
		private const val USUARIO_SEED = 1L

		private val REQUEST_OUTUBRO = """
			{
				"mesReferencia": "2026-10-01",
				"parametrosCalculo": {
					"versaoEsquema": 1,
					"percentuais": { "essenciais": 0.50, "lazer": 0.30 },
					"tetos": { "essenciais": "1300.00", "lazer": "700.00" }
				}
			}
		""".trimIndent()
	}

	// AC #1 + #8: sumário e distribuição derivados dos lançamentos, com dinheiro
	// serializado como string.
	@Test
	fun `sumario derivado dos lancamentos do mes com dinheiro como string`() {
		val id = criarMes()
		inserirLancamento(id, "RECEITA", "2000.00")
		inserirLancamento(id, "DESPESA", "500.00", pago = true)
		inserirLancamento(id, "DESPESA", "306.92")

		mockMvc.get("/api/v1/meses/$id/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.receitas") { isString() } }
			.andExpect { jsonPath("$.receitas") { value("2000.00") } }
			.andExpect { jsonPath("$.despesas") { value("806.92") } }
			.andExpect { jsonPath("$.aPagar") { value("306.92") } }
			.andExpect { jsonPath("$.restante") { value("1193.08") } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { isString() } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("596.54") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("357.92") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("238.62") } }
			.andExpect { jsonPath("$.distribuicao.deficit") { value("0.00") } }
	}

	// AC #2: mês sem lançamentos retorna tudo zero.
	@Test
	fun `mes sem lancamentos retorna sumario e distribuicao zerados`() {
		val id = criarMes()

		mockMvc.get("/api/v1/meses/$id/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.receitas") { value("0.00") } }
			.andExpect { jsonPath("$.despesas") { value("0.00") } }
			.andExpect { jsonPath("$.aPagar") { value("0.00") } }
			.andExpect { jsonPath("$.restante") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.deficit") { value("0.00") } }
	}

	// AC #4 + #7: restante <= 0 zera as categorias e expõe o déficit.
	@Test
	fun `restante negativo zera categorias e expoe deficit igual ao valor absoluto`() {
		val id = criarMes()
		inserirLancamento(id, "RECEITA", "1000.00")
		inserirLancamento(id, "DESPESA", "1500.00", pago = true)

		mockMvc.get("/api/v1/meses/$id/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.restante") { value("-500.00") } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.deficit") { value("500.00") } }
	}

	// AC #3: a distribuição aplica as alternâncias gravadas no mês.
	@Test
	fun `alternancia investir desligada no mes muda a distribuicao`() {
		val id = criarMes(
			"""
			{
				"mesReferencia": "2026-10-01",
				"parametrosCalculo": {
					"versaoEsquema": 1,
					"percentuais": { "essenciais": 0.50, "lazer": 0.30 },
					"tetos": { "essenciais": "1300.00", "lazer": "700.00" }
				},
				"alternarInvestir": false,
				"alternarLazer": true
			}
			""".trimIndent(),
		)
		inserirLancamento(id, "RECEITA", "1193.08")

		mockMvc.get("/api/v1/meses/$id/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("1014.12") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("178.96") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.deficit") { value("0.00") } }
	}

	@Test
	fun `alternancia lazer desligada no mes muda a distribuicao`() {
		val id = criarMes(
			"""
			{
				"mesReferencia": "2026-11-01",
				"parametrosCalculo": {
					"versaoEsquema": 1,
					"percentuais": { "essenciais": 0.50, "lazer": 0.30 },
					"tetos": { "essenciais": "1300.00", "lazer": "700.00" }
				},
				"alternarInvestir": true,
				"alternarLazer": false
			}
			""".trimIndent(),
		)
		inserirLancamento(id, "RECEITA", "1193.08")

		mockMvc.get("/api/v1/meses/$id/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("894.81") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("298.27") } }
			.andExpect { jsonPath("$.distribuicao.deficit") { value("0.00") } }
	}

	// AC #5: alterar um lançamento reflete em nova chamada ao endpoint.
	@Test
	fun `alterar um lancamento do mes reflete na proxima chamada`() {
		val id = criarMes()
		inserirLancamento(id, "RECEITA", "1000.00")

		mockMvc.get("/api/v1/meses/$id/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.receitas") { value("1000.00") } }
			.andExpect { jsonPath("$.restante") { value("1000.00") } }

		jdbcTemplate.update(
			"""
			UPDATE lancamentos SET valor = ?
			WHERE mes_id = (SELECT id FROM meses WHERE id_publico = ?) AND tipo = 'RECEITA'
			""".trimIndent(),
			BigDecimal("1500.00"),
			UUID.fromString(id),
		)

		mockMvc.get("/api/v1/meses/$id/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.receitas") { value("1500.00") } }
			.andExpect { jsonPath("$.restante") { value("1500.00") } }
	}

	// AC #6: a distribuição usa o snapshot de parâmetros gravado no próprio mês;
	// dois meses com snapshots diferentes produzem distribuições diferentes com
	// os mesmos lançamentos.
	@Test
	fun `distribuicao usa o snapshot de parametros do proprio mes`() {
		val idOutubro = criarMes()
		val requestNovembro = """
			{
				"mesReferencia": "2026-11-01",
				"parametrosCalculo": {
					"versaoEsquema": 1,
					"percentuais": { "essenciais": 0.40, "lazer": 0.20 },
					"tetos": { "essenciais": "900.00", "lazer": "400.00" }
				}
			}
		""".trimIndent()
		val idNovembro = criarMes(requestNovembro)
		inserirLancamento(idOutubro, "RECEITA", "1193.08")
		inserirLancamento(idNovembro, "RECEITA", "1193.08")

		mockMvc.get("/api/v1/meses/$idOutubro/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("596.54") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("357.92") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("238.62") } }

		mockMvc.get("/api/v1/meses/$idNovembro/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("477.23") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("238.62") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("477.23") } }
	}

	@Test
	fun `sumario de mes inexistente ou de outro usuario retorna 404`() {
		val id = criarMes()
		jdbcTemplate.update(
			"INSERT INTO usuarios (id, sujeito_provedor) OVERRIDING SYSTEM VALUE VALUES (2, 'outro-usuario')",
		)

		mockMvc.get("/api/v1/meses/00000000-0000-0000-0000-000000000000/sumario")
			.andExpect { status { isNotFound() } }

		mockMvc.get("/api/v1/meses/$id/sumario") {
			header(UsuarioAutenticadoService.HEADER_USUARIO_ID, "2")
		}
			.andExpect { status { isNotFound() } }
	}

	private fun criarMes(request: String = REQUEST_OUTUBRO): String {
		val resposta = mockMvc.post("/api/v1/meses") {
			contentType = MediaType.APPLICATION_JSON
			content = request
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
	}

	private fun inserirLancamento(
		mesIdPublico: String,
		tipo: String,
		valor: String,
		pago: Boolean = false,
	) {
		val mesId = jdbcTemplate.queryForObject(
			"SELECT id FROM meses WHERE id_publico = ?",
			Long::class.java,
			UUID.fromString(mesIdPublico),
		)!!
		jdbcTemplate.update(
			"""
			INSERT INTO lancamentos (id_publico, usuario_id, mes_id, tipo, descricao, valor, pago)
			VALUES (?, ?, ?, ?::tipo_lancamento, 'lançamento de teste', ?, ?)
			""".trimIndent(),
			UUID.randomUUID(),
			USUARIO_SEED,
			mesId,
			tipo,
			BigDecimal(valor),
			pago,
		)
	}
}
