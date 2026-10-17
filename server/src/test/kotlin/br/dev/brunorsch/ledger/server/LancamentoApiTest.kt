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
class LancamentoApiTest : SuportePostgres() {

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

		private val REQUEST_NOVEMBRO = """
			{
				"mesReferencia": "2026-11-01",
				"parametrosCalculo": {
					"versaoEsquema": 1,
					"percentuais": { "essenciais": 0.50, "lazer": 0.30 },
					"tetos": { "essenciais": "1300.00", "lazer": "700.00" }
				}
			}
		""".trimIndent()
	}

	// AC #1: POST cria despesa com pago=false por padrão e retorna 201.
	@Test
	fun `post cria despesa com pago false por padrao e retorna 201`() {
		val mesId = criarMes()

		mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"tipo": "DESPESA", "descricao": "mercado", "valor": "250.50"}"""
		}
			.andExpect { status { isCreated() } }
			.andExpect { jsonPath("$.id") { isNotEmpty() } }
			.andExpect { jsonPath("$.tipo") { value("DESPESA") } }
			.andExpect { jsonPath("$.descricao") { value("mercado") } }
			.andExpect { jsonPath("$.valor") { isString() } }
			.andExpect { jsonPath("$.valor") { value("250.50") } }
			.andExpect { jsonPath("$.pago") { value(false) } }
			.andExpect { jsonPath("$.pagoEm") { value(null) } }
			.andExpect { jsonPath("$.origemGastoFixo") { value(false) } }
	}

	// AC #2: POST cria receita com pago ignorado e retorna 201 sem pago/pagoEm.
	@Test
	fun `post cria receita com pago ignorado e retorna 201`() {
		val mesId = criarMes()

		val resposta = mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			contentType = MediaType.APPLICATION_JSON
			content = """
				{"tipo": "RECEITA", "descricao": "salário", "valor": "5000.00",
				 "pago": true, "pagoEm": "2026-10-01T10:00:00Z"}
			""".trimIndent()
		}
			.andExpect { status { isCreated() } }
			.andExpect { jsonPath("$.tipo") { value("RECEITA") } }
			.andExpect { jsonPath("$.descricao") { value("salário") } }
			.andExpect { jsonPath("$.valor") { value("5000.00") } }
			.andExpect { jsonPath("$.pago").doesNotExist() }
			.andExpect { jsonPath("$.pagoEm").doesNotExist() }
			.andReturn()

		val idPublico = objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
		val pagoGravado = jdbcTemplate.queryForObject(
			"SELECT pago FROM lancamentos WHERE id_publico = ?",
			Boolean::class.java,
			UUID.fromString(idPublico),
		)
		assertThat(pagoGravado).isFalse()
	}

	// AC #3: POST com valor <= 0 retorna 400.
	@Test
	fun `post com valor zero retorna 400 com mensagem clara`() {
		val mesId = criarMes()

		mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"tipo": "DESPESA", "descricao": "mercado", "valor": "0.00"}"""
		}
			.andExpect { status { isBadRequest() } }
			.andExpect { jsonPath("$.mensagem") { value("valor deve ser maior que zero: 0.00") } }
	}

	@Test
	fun `post com valor negativo retorna 400`() {
		val mesId = criarMes()

		mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"tipo": "DESPESA", "descricao": "mercado", "valor": "-5.00"}"""
		}
			.andExpect { status { isBadRequest() } }
	}

	// AC #4: POST em mês de outro usuário retorna 404.
	@Test
	fun `post em mes de outro usuario retorna 404`() {
		val mesId = criarMes()
		criarUsuario2()

		mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"tipo": "DESPESA", "descricao": "mercado", "valor": "10.00"}"""
			header(UsuarioAutenticadoService.HEADER_USUARIO_ID, "2")
		}
			.andExpect { status { isNotFound() } }
	}

	// AC #5: GET lista apenas lançamentos do mês e do usuario_id da request.
	@Test
	fun `get lista apenas lancamentos do mes e do usuario da request`() {
		val mesOutubro = criarMes()
		val mesNovembro = criarMes(REQUEST_NOVEMBRO)
		criarLancamento(mesOutubro, "RECEITA", "1000.00")
		criarLancamento(mesOutubro, "DESPESA", "100.00")
		criarLancamento(mesNovembro, "RECEITA", "2000.00")

		mockMvc.get("/api/v1/meses/$mesOutubro/lancamentos")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$") { isArray() } }
			.andExpect { jsonPath("$.length()") { value(2) } }
			.andExpect { jsonPath("$[0].tipo") { value("RECEITA") } }
			.andExpect { jsonPath("$[0].valor") { value("1000.00") } }
			.andExpect { jsonPath("$[1].tipo") { value("DESPESA") } }
			.andExpect { jsonPath("$[1].valor") { value("100.00") } }

		mockMvc.get("/api/v1/meses/$mesNovembro/lancamentos")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(1) } }
	}

	@Test
	fun `get lancamentos de mes de outro usuario retorna 404`() {
		val mesId = criarMes()
		criarUsuario2()

		mockMvc.get("/api/v1/meses/$mesId/lancamentos") {
			header(UsuarioAutenticadoService.HEADER_USUARIO_ID, "2")
		}
			.andExpect { status { isNotFound() } }
	}

	// AC #6: PATCH em pago=true preenche pagoEm automaticamente quando ausente.
	@Test
	fun `patch pago true preenche pagoEm automaticamente quando ausente`() {
		val mesId = criarMes()
		val lancamentoId = criarLancamento(mesId, "DESPESA", "100.00")

		mockMvc.patch("/api/v1/lancamentos/$lancamentoId") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"pago": true}"""
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.pago") { value(true) } }
			.andExpect { jsonPath("$.pagoEm") { isNotEmpty() } }

		val pagoEmGravado = jdbcTemplate.queryForObject(
			"SELECT pago_em FROM lancamentos WHERE id_publico = ?",
			java.sql.Timestamp::class.java,
			UUID.fromString(lancamentoId),
		)
		assertThat(pagoEmGravado).isNotNull()
	}

	// AC #7: PATCH em pago=false limpa pagoEm.
	@Test
	fun `patch pago false limpa pagoEm`() {
		val mesId = criarMes()
		val lancamentoId = criarLancamento(
			mesId,
			"DESPESA",
			"100.00",
			pago = true,
			pagoEm = "2026-10-01T10:00:00Z",
		)

		mockMvc.patch("/api/v1/lancamentos/$lancamentoId") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"pago": false}"""
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.pago") { value(false) } }
			.andExpect { jsonPath("$.pagoEm") { value(null) } }
	}

	@Test
	fun `patch em lancamento de outro usuario ou inexistente retorna 404`() {
		val mesId = criarMes()
		val lancamentoId = criarLancamento(mesId, "DESPESA", "100.00")
		criarUsuario2()

		mockMvc.patch("/api/v1/lancamentos/$lancamentoId") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"pago": true}"""
			header(UsuarioAutenticadoService.HEADER_USUARIO_ID, "2")
		}
			.andExpect { status { isNotFound() } }

		mockMvc.patch("/api/v1/lancamentos/00000000-0000-0000-0000-000000000000") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"pago": true}"""
		}
			.andExpect { status { isNotFound() } }
	}

	// AC #8: DELETE remove o lançamento e retorna 204.
	@Test
	fun `delete remove o lancamento e retorna 204`() {
		val mesId = criarMes()
		val lancamentoId = criarLancamento(mesId, "RECEITA", "1000.00")

		mockMvc.delete("/api/v1/lancamentos/$lancamentoId")
			.andExpect { status { isNoContent() } }

		mockMvc.get("/api/v1/meses/$mesId/lancamentos")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.length()") { value(0) } }
	}

	@Test
	fun `delete de lancamento inexistente ou de outro usuario retorna 404`() {
		val mesId = criarMes()
		val lancamentoId = criarLancamento(mesId, "DESPESA", "100.00")
		criarUsuario2()

		mockMvc.delete("/api/v1/lancamentos/$lancamentoId") {
			header(UsuarioAutenticadoService.HEADER_USUARIO_ID, "2")
		}
			.andExpect { status { isNotFound() } }

		mockMvc.delete("/api/v1/lancamentos/00000000-0000-0000-0000-000000000000")
			.andExpect { status { isNotFound() } }
	}

	// AC #9: mudanças refletem no sumário do mês.
	@Test
	fun `mudancas nos lancamentos refletem no sumario do mes`() {
		val mesId = criarMes()
		val lancamentoId = criarLancamento(mesId, "RECEITA", "1000.00")

		mockMvc.get("/api/v1/meses/$mesId/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.receitas") { value("1000.00") } }

		mockMvc.patch("/api/v1/lancamentos/$lancamentoId") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"valor": "1500.00"}"""
		}
			.andExpect { status { isOk() } }

		mockMvc.get("/api/v1/meses/$mesId/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.receitas") { value("1500.00") } }

		mockMvc.delete("/api/v1/lancamentos/$lancamentoId")
			.andExpect { status { isNoContent() } }

		mockMvc.get("/api/v1/meses/$mesId/sumario")
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.receitas") { value("0.00") } }
	}

	// AC #10: dinheiro serializado como string no JSON de request e resposta.
	@Test
	fun `dinheiro e serializado como string na request e na resposta`() {
		val mesId = criarMes()

		mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"tipo": "RECEITA", "descricao": "freelance", "valor": "25.50"}"""
		}
			.andExpect { status { isCreated() } }
			.andExpect { jsonPath("$.valor") { isString() } }
			.andExpect { jsonPath("$.valor") { value("25.50") } }
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

	private fun criarUsuario2() {
		jdbcTemplate.update(
			"INSERT INTO usuarios (id, sujeito_provedor) OVERRIDING SYSTEM VALUE VALUES (2, 'outro-usuario')",
		)
	}

	private fun criarLancamento(
		mesId: String,
		tipo: String,
		valor: String,
		pago: Boolean? = null,
		pagoEm: String? = null,
	): String {
		val campos = mutableListOf(
			""""tipo": "$tipo"""",
			""""descricao": "lançamento de teste"""",
			""""valor": "$valor"""",
		)
		if (pago != null) campos += """"pago": $pago"""
		if (pagoEm != null) campos += """"pagoEm": "$pagoEm""""

		val resposta = mockMvc.post("/api/v1/meses/$mesId/lancamentos") {
			contentType = MediaType.APPLICATION_JSON
			content = "{ ${campos.joinToString(", ")} }"
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].stringValue()
	}
}
