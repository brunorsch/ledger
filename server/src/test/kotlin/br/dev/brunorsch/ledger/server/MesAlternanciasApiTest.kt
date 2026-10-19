package br.dev.brunorsch.ledger.server

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
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
class MesAlternanciasApiTest : SuportePostgres() {

	@Autowired
	lateinit var mockMvc: MockMvc

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	@Autowired
	lateinit var objectMapper: JsonMapper

	companion object {
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

	// AC #1: PATCH aceita as alternâncias e retorna 200 com os valores
	// atualizados, persistidos no mês.
	@Test
	fun `patch altera alternancias e retorna 200 com valores persistidos`() {
		val id = criarMes()

		mockMvc.patch("/api/v1/meses/$id/alternancias") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"alternarInvestir": false, "alternarLazer": false}"""
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.alternarInvestir") { value(false) } }
			.andExpect { jsonPath("$.alternarLazer") { value(false) } }

		mockMvc.get("/api/v1/meses/$id") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.alternarInvestir") { value(false) } }
			.andExpect { jsonPath("$.alternarLazer") { value(false) } }
	}

	// Campos são opcionais: o que não for enviado mantém o valor atual.
	@Test
	fun `campo nao enviado mantem o valor atual do mes`() {
		val id = criarMes()

		mockMvc.patch("/api/v1/meses/$id/alternancias") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"alternarInvestir": false}"""
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.alternarInvestir") { value(false) } }
			.andExpect { jsonPath("$.alternarLazer") { value(true) } }

		mockMvc.patch("/api/v1/meses/$id/alternancias") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"alternarLazer": false}"""
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.alternarInvestir") { value(false) } }
			.andExpect { jsonPath("$.alternarLazer") { value(false) } }
	}

	// AC #2: a alternância de essenciais nunca pode ser desligada. O payload
	// não expõe campo de essenciais (constraint estrutural, mesma resolução do
	// AC #8 da TASK-4) — mesmo com investir e lazer desligadas, o restante vai
	// todo para essenciais na distribuição.
	@Test
	fun `essenciais nunca desligada mesmo com investir e lazer desligadas`() {
		val id = criarMes()
		inserirLancamento(id, "RECEITA", "1193.08")

		mockMvc.patch("/api/v1/meses/$id/alternancias") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"alternarInvestir": false, "alternarLazer": false}"""
		}
			.andExpect { status { isOk() } }

		mockMvc.get("/api/v1/meses/$id/sumario") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("1193.08") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("0.00") } }
			.andExpect { jsonPath("$.distribuicao.deficit") { value("0.00") } }
	}

	// AC #3: alterar as alternâncias reflete imediatamente na distribuição
	// obtida por GET /meses/{id}/sumario.
	@Test
	fun `alterar alternancias reflete na distribuicao do sumario`() {
		val id = criarMes()
		inserirLancamento(id, "RECEITA", "1193.08")

		mockMvc.get("/api/v1/meses/$id/sumario") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("596.54") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("357.92") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("238.62") } }

		mockMvc.patch("/api/v1/meses/$id/alternancias") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"alternarInvestir": false}"""
		}
			.andExpect { status { isOk() } }

		mockMvc.get("/api/v1/meses/$id/sumario") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.distribuicao.essenciais") { value("1014.12") } }
			.andExpect { jsonPath("$.distribuicao.lazer") { value("178.96") } }
			.andExpect { jsonPath("$.distribuicao.reservas") { value("0.00") } }
	}

	// AC #4: mês de outro usuário retorna 404 (autorização por usuario_id).
	@Test
	fun `mes de outro usuario retorna 404`() {
		val id = criarMes()

		mockMvc.patch("/api/v1/meses/$id/alternancias") {
			comAutorizacao(TokenTestes.UID_OUTRO)
			contentType = MediaType.APPLICATION_JSON
			content = """{"alternarInvestir": false}"""
		}
			.andExpect { status { isNotFound() } }
	}

	// AC #5: mês inexistente retorna 404.
	@Test
	fun `mes inexistente retorna 404`() {
		mockMvc.patch("/api/v1/meses/00000000-0000-0000-0000-000000000000/alternancias") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"alternarInvestir": false}"""
		}
			.andExpect { status { isNotFound() } }
	}

	private fun criarMes(request: String = REQUEST_OUTUBRO): String {
		val resposta = mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = request
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
	}

	private fun inserirLancamento(mesIdPublico: String, tipo: String, valor: String) {
		val mesId = jdbcTemplate.queryForObject(
			"SELECT id FROM meses WHERE id_publico = ?",
			Long::class.java,
			UUID.fromString(mesIdPublico),
		)!!
		jdbcTemplate.update(
			"""
			INSERT INTO lancamentos (id_publico, usuario_id, mes_id, tipo, descricao, valor, pago)
			VALUES (?, ?, ?, ?::tipo_lancamento, 'lançamento de teste', ?, true)
			""".trimIndent(),
			UUID.randomUUID(),
			TokenTestes.usuarioId(jdbcTemplate, TokenTestes.UID_PADRAO),
			mesId,
			tipo,
			BigDecimal(valor),
		)
	}
}
