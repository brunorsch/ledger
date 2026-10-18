package br.dev.brunorsch.ledger.server

import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
import org.assertj.core.api.Assertions.assertThat
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
class GastoFixoApiTest : SuportePostgres() {

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

	// AC #1: POST cria gasto fixo ativo por padrão e retorna 201.
	@Test
	fun `post cria gasto fixo com ativo true por padrao e retorna 201`() {
		mockMvc.post("/api/v1/gastos-fixos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "aluguel", "valorPadrao": "250.50"}"""
		}
			.andExpect { status { isCreated() } }
			.andExpect { jsonPath("$.id") { isNotEmpty() } }
			.andExpect { jsonPath("$.descricao") { value("aluguel") } }
			.andExpect { jsonPath("$.valorPadrao") { value("250.50") } }
			.andExpect { jsonPath("$.ativo") { value(true) } }
	}

	// AC #2: POST aceita valorPadrao zero e rejeita negativo.
	@Test
	fun `post com valor zero retorna 201`() {
		mockMvc.post("/api/v1/gastos-fixos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "sem valor", "valorPadrao": "0.00"}"""
		}
			.andExpect { status { isCreated() } }
			.andExpect { jsonPath("$.valorPadrao") { value("0.00") } }
	}

	@Test
	fun `post com valor negativo retorna 400`() {
		mockMvc.post("/api/v1/gastos-fixos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "aluguel", "valorPadrao": "-5.00"}"""
		}
			.andExpect { status { isBadRequest() } }
	}

	// AC #3: GET lista apenas gastos fixos ativos do usuário.
	@Test
	fun `get lista apenas gastos fixos ativos do usuario`() {
		val ativo1 = criarGastoFixo("aluguel", "250.50")
		criarGastoFixo("internet", "120.00")
		val desativado = criarGastoFixo("academia", "90.00")
		mockMvc.delete("/api/v1/gastos-fixos/$desativado")
			.andExpect { status { isNoContent() } }

		criarUsuario2()
		criarGastoFixo("aluguel do outro", "999.00", usuarioIdHeader = "2")

		assertThat(ativo1).isNotEmpty()

		mockMvc.get("/api/v1/gastos-fixos")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$") { isArray() } }
			.andExpect { jsonPath("$.length()") { value(2) } }
			.andExpect { jsonPath("$[0].descricao") { value("aluguel") } }
			.andExpect { jsonPath("$[1].descricao") { value("internet") } }
	}

	// AC #4: PATCH valida valor, altera parcialmente e trata inexistente/outro usuário.
	@Test
	fun `patch com valor negativo retorna 400`() {
		val gastoFixoId = criarGastoFixo("aluguel", "250.50")

		mockMvc.patch("/api/v1/gastos-fixos/$gastoFixoId") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"valorPadrao": "-5.00"}"""
		}
			.andExpect { status { isBadRequest() } }
	}

	@Test
	fun `patch altera descricao preservando valor nao enviado`() {
		val gastoFixoId = criarGastoFixo("aluguel", "250.50")

		mockMvc.patch("/api/v1/gastos-fixos/$gastoFixoId") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "aluguel novo"}"""
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.descricao") { value("aluguel novo") } }
			.andExpect { jsonPath("$.valorPadrao") { value("250.50") } }
			.andExpect { jsonPath("$.ativo") { value(true) } }
	}

	@Test
	fun `patch em gasto fixo inexistente ou de outro usuario retorna 404`() {
		val gastoFixoId = criarGastoFixo("aluguel", "250.50")
		criarUsuario2()

		mockMvc.patch("/api/v1/gastos-fixos/$gastoFixoId") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "tentativa"}"""
			header(UsuarioAutenticadoService.HEADER_USUARIO_ID, "2")
		}
			.andExpect { status { isNotFound() } }

		mockMvc.patch("/api/v1/gastos-fixos/00000000-0000-0000-0000-000000000000") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "tentativa"}"""
		}
			.andExpect { status { isNotFound() } }
	}

	// AC #5: DELETE faz soft delete.
	@Test
	fun `delete desativa gasto fixo e retorna 204`() {
		val gastoFixoId = criarGastoFixo("aluguel", "250.50")

		mockMvc.delete("/api/v1/gastos-fixos/$gastoFixoId")
			.andExpect { status { isNoContent() } }

		mockMvc.get("/api/v1/gastos-fixos")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(0) } }

		val ativoGravado = jdbcTemplate.queryForObject(
			"SELECT ativo FROM gastos_fixos WHERE id_publico = ?",
			Boolean::class.java,
			UUID.fromString(gastoFixoId),
		)
		assertThat(ativoGravado).isFalse()
	}

	@Test
	fun `delete de gasto fixo inexistente ou de outro usuario retorna 404`() {
		val gastoFixoId = criarGastoFixo("aluguel", "250.50")
		criarUsuario2()

		mockMvc.delete("/api/v1/gastos-fixos/$gastoFixoId") {
			header(UsuarioAutenticadoService.HEADER_USUARIO_ID, "2")
		}
			.andExpect { status { isNotFound() } }

		mockMvc.delete("/api/v1/gastos-fixos/00000000-0000-0000-0000-000000000000")
			.andExpect { status { isNotFound() } }
	}

	// AC #6: mudanças no gasto fixo não afetam lançamentos de meses já criados.
	@Test
	fun `mudanca em gasto fixo nao altera lancamentos de meses criados`() {
		val mesId = criarMes()
		criarLancamento(mesId, "mercado", "250.50")

		val gastoFixoId = criarGastoFixo("mercado", "250.50")

		mockMvc.patch("/api/v1/gastos-fixos/$gastoFixoId") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "mercado novo", "valorPadrao": "999.00"}"""
		}
			.andExpect { status { isOk() } }

		mockMvc.delete("/api/v1/gastos-fixos/$gastoFixoId")
			.andExpect { status { isNoContent() } }

		mockMvc.get("/api/v1/meses/$mesId/lancamentos")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(1) } }
			.andExpect { jsonPath("$[0].descricao") { value("mercado") } }
			.andExpect { jsonPath("$[0].valor") { value("250.50") } }
	}

	// AC #7: dinheiro serializado como string na request e na resposta.
	@Test
	fun `dinheiro e serializado como string na request e na resposta`() {
		val resposta = mockMvc.post("/api/v1/gastos-fixos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "aluguel", "valorPadrao": "25.50"}"""
		}
			.andExpect { status { isCreated() } }
			.andExpect { jsonPath("$.valorPadrao") { isString() } }
			.andExpect { jsonPath("$.valorPadrao") { value("25.50") } }
			.andReturn()

		assertThat(resposta.response.contentAsString).isNotBlank()

		mockMvc.get("/api/v1/gastos-fixos")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$[0].valorPadrao") { isString() } }
			.andExpect { jsonPath("$[0].valorPadrao") { value("25.50") } }
	}

	private fun criarGastoFixo(descricao: String, valorPadrao: String, usuarioIdHeader: String? = null): String {
		val resposta = mockMvc.post("/api/v1/gastos-fixos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"descricao": "$descricao", "valorPadrao": "$valorPadrao"}"""
			if (usuarioIdHeader != null) header(UsuarioAutenticadoService.HEADER_USUARIO_ID, usuarioIdHeader)
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
	}

	private fun criarMes(): String {
		val resposta = mockMvc.post("/api/v1/meses") {
			contentType = MediaType.APPLICATION_JSON
			content = REQUEST_OUTUBRO
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
	}

	private fun criarLancamento(mesId: String, descricao: String, valor: String): String {
		val resposta = mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"tipo": "DESPESA", "descricao": "$descricao", "valor": "$valor"}"""
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
	}

	private fun criarUsuario2() {
		jdbcTemplate.update(
			"INSERT INTO usuarios (id, identity_provider) OVERRIDING SYSTEM VALUE VALUES (2, 'outro-usuario')",
		)
	}
}
