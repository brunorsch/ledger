package br.dev.brunorsch.ledger.server

import br.dev.brunorsch.ledger.server.service.MesService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MesCriacaoGastosFixosApiTest : SuportePostgres() {

	@Autowired
	lateinit var mockMvc: MockMvc

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	@Autowired
	lateinit var objectMapper: JsonMapper

	@Autowired
	lateinit var mesService: MesService

	// AC #1: mês criado sem gastos fixos ativos nasce sem lançamentos.
	@Test
	fun `mes criado sem gastos fixos ativos nasce sem lancamentos`() {
		val mesId = criarMes()

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$") { isArray() } }
			.andExpect { jsonPath("$.length()") { value(0) } }
	}

	// AC #2: mês criado gera uma despesa não paga por gasto fixo ativo.
	@Test
	fun `mes criado gera uma despesa nao paga por gasto fixo ativo`() {
		criarGastoFixo("aluguel", "250.50")
		criarGastoFixo("internet", "120.00")

		val mesId = criarMes()

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(2) } }
			.andExpect { jsonPath("$[0].tipo") { value("DESPESA") } }
			.andExpect { jsonPath("$[0].pago") { value(false) } }
			.andExpect { jsonPath("$[0].origemGastoFixo") { value(true) } }
			.andExpect { jsonPath("$[1].tipo") { value("DESPESA") } }
			.andExpect { jsonPath("$[1].pago") { value(false) } }
			.andExpect { jsonPath("$[1].origemGastoFixo") { value(true) } }
	}

	// AC #3: lançamentos gerados copiam descrição e valor do gasto fixo de origem.
	@Test
	fun `lancamentos gerados copiam descricao e valor do gasto fixo de origem`() {
		criarGastoFixo("aluguel", "250.50")
		criarGastoFixo("internet", "120.00")

		val mesId = criarMes()

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$[0].descricao") { value("aluguel") } }
			.andExpect { jsonPath("$[0].valor") { value("250.50") } }
			.andExpect { jsonPath("$[0].origemGastoFixo") { value(true) } }
			.andExpect { jsonPath("$[1].descricao") { value("internet") } }
			.andExpect { jsonPath("$[1].valor") { value("120.00") } }
			.andExpect { jsonPath("$[1].origemGastoFixo") { value(true) } }
	}

	// AC #4: alterar o gasto fixo depois não muda a cópia já gerada no mês.
	@Test
	fun `alterar gasto fixo nao muda lancamento ja gerado no mes`() {
		val gastoFixoId = criarGastoFixo("aluguel", "250.50")
		val mesId = criarMes()

		mockMvc.patch("/api/v1/gastos-fixos/$gastoFixoId") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "aluguel novo", "valorPadrao": "999.00"}"""
		}
			.andExpect { status { isOk() } }

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(1) } }
			.andExpect { jsonPath("$[0].descricao") { value("aluguel") } }
			.andExpect { jsonPath("$[0].valor") { value("250.50") } }
	}

	// AC #5: gasto fixo criado depois do mês não gera lançamento nele.
	@Test
	fun `gasto fixo criado depois do mes nao gera lancamento nele`() {
		val mesId = criarMes()

		criarGastoFixo("aluguel", "250.50")

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(0) } }
	}

	// AC #6: gasto fixo desativado não gera lançamento na criação do mês.
	@Test
	fun `gasto fixo desativado nao gera lancamento na criacao do mes`() {
		criarGastoFixo("aluguel", "250.50")
		val desativado = criarGastoFixo("academia", "90.00")
		mockMvc.delete("/api/v1/gastos-fixos/$desativado") {
			comAutorizacao()
		}
			.andExpect { status { isNoContent() } }

		val mesId = criarMes()

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(1) } }
			.andExpect { jsonPath("$[0].descricao") { value("aluguel") } }
	}

	// AC #7: gerar lançamentos de gastos fixos é idempotente por mês.
	@Test
	fun `gerar lancamentos de gastos fixos nao duplica em mes ja populado`() {
		criarGastoFixo("aluguel", "250.50")
		criarGastoFixo("internet", "120.00")
		val mesId = criarMes()
		val mesIdInterno = jdbcTemplate.queryForObject(
			"SELECT id FROM meses WHERE id_publico = ?",
			Long::class.java,
			UUID.fromString(mesId),
		)!!

		mesService.gerarLancamentosDeGastosFixos(mesIdInterno, usuarioIdPadrao())

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(2) } }
	}

	// Revisão: mês com lançamento manual ainda gera os gastos fixos quando a rotina roda.
	@Test
	fun `mes com lancamento manual ainda gera lancamentos de gastos fixos`() {
		val mesId = criarMes()
		val mesIdInterno = jdbcTemplate.queryForObject(
			"SELECT id FROM meses WHERE id_publico = ?",
			Long::class.java,
			UUID.fromString(mesId),
		)!!
		criarLancamentoManual(mesId)
		criarGastoFixo("aluguel", "250.50")

		mesService.gerarLancamentosDeGastosFixos(mesIdInterno, usuarioIdPadrao())

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(2) } }
			.andExpect { jsonPath("$[0].descricao") { value("lançamento manual") } }
			.andExpect { jsonPath("$[0].origemGastoFixo") { value(false) } }
			.andExpect { jsonPath("$[1].descricao") { value("aluguel") } }
			.andExpect { jsonPath("$[1].origemGastoFixo") { value(true) } }

		mesService.gerarLancamentosDeGastosFixos(mesIdInterno, usuarioIdPadrao())

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(2) } }
	}

	// Mês duplicado: recriar a mesma referência para o usuário retorna 409.
	@Test
	fun `criar mes com referencia ja existente retorna 409`() {
		criarMes("2026-10-01")

		mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = requestMes("2026-10-01")
		}
			.andExpect { status { isConflict() } }
			.andExpect { jsonPath("$.mensagem") { isNotEmpty() } }
	}

	private fun usuarioIdPadrao(): Long = TokenTestes.usuarioId(jdbcTemplate, TokenTestes.UID_PADRAO)

	private fun criarGastoFixo(descricao: String, valorPadrao: String): String {
		val resposta = mockMvc.post("/api/v1/gastos-fixos") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "$descricao", "valorPadrao": "$valorPadrao"}"""
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
	}

	private fun criarLancamentoManual(mesId: String) {
		mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = """{"tipo": "DESPESA", "descricao": "lançamento manual", "valor": "10.00"}"""
		}
			.andExpect { status { isCreated() } }
	}

	private fun criarMes(mesReferencia: String = "2026-10-01"): String {
		val resposta = mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = requestMes(mesReferencia)
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
	}

	private fun requestMes(mesReferencia: String): String = """
		{
			"mesReferencia": "$mesReferencia",
			"parametrosCalculo": {
				"versaoEsquema": 1,
				"percentuais": { "essenciais": 0.50, "lazer": 0.30 },
				"tetos": { "essenciais": "1300.00", "lazer": "700.00" }
			}
		}
	""".trimIndent()
}
