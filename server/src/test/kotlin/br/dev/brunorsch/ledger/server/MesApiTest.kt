package br.dev.brunorsch.ledger.server

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MesApiTest : SuportePostgres() {

	@Autowired
	lateinit var mockMvc: MockMvc

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

	@Test
	fun `post cria mes e retorna 201 com o payload completo`() {
		mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = REQUEST_OUTUBRO
		}
			.andExpect { status { isCreated() } }
			.andExpect { jsonPath("$.id") { isNotEmpty() } }
			.andExpect { jsonPath("$.mesReferencia") { value("2026-10-01") } }
			.andExpect { jsonPath("$.parametrosCalculo.versaoEsquema") { value(1) } }
			.andExpect { jsonPath("$.alternarInvestir") { value(true) } }
			.andExpect { jsonPath("$.alternarLazer") { value(true) } }
			.andExpect { jsonPath("$.deficit") { value("0.00") } }
			.andExpect { jsonPath("$.criadoEm") { isNotEmpty() } }
	}

	@Test
	fun `parametros de calculo gravados sao iguais aos enviados na request`() {
		val resposta = mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = REQUEST_OUTUBRO
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		val corpoCriacao = objectMapper.readTree(resposta.response.contentAsString)
		val id = corpoCriacao["id"].asText()

		val leitura = mockMvc.get("/api/v1/meses/$id") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andReturn()

		val corpoLeitura = objectMapper.readTree(leitura.response.contentAsString)
		assertThat(corpoLeitura["parametrosCalculo"])
			.isEqualTo(objectMapper.readTree(REQUEST_OUTUBRO)["parametrosCalculo"])
	}

	@Test
	fun `mes com mes_referencia fora do primeiro dia retorna 400 com mensagem clara`() {
		val request = REQUEST_OUTUBRO.replace("2026-10-01", "2026-10-15")

		mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = request
		}
			.andExpect { status { isBadRequest() } }
			.andExpect { jsonPath("$.mensagem") { value("mes_referencia deve ser o primeiro dia do mês: 2026-10-15") } }
	}

	@Test
	fun `mes sem versao_esquema nos parametros de calculo retorna 400`() {
		val request = """
			{
				"mesReferencia": "2026-10-01",
				"parametrosCalculo": {
					"percentuais": { "essenciais": 0.50, "lazer": 0.30 },
					"tetos": { "essenciais": "1300.00", "lazer": "700.00" }
				}
			}
		""".trimIndent()

		mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = request
		}
			.andExpect { status { isBadRequest() } }
	}

	@Test
	fun `get por id retorna 200 para mes do proprio usuario e 404 para mes inexistente`() {
		val id = criarMes(REQUEST_OUTUBRO)

		mockMvc.get("/api/v1/meses/$id") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.id") { value(id) } }

		mockMvc.get("/api/v1/meses/00000000-0000-0000-0000-000000000000") {
			comAutorizacao()
		}
			.andExpect { status { isNotFound() } }
	}

	@Test
	fun `usuario nao consegue ler mes de outro usuario`() {
		val id = criarMes(REQUEST_OUTUBRO)

		mockMvc.get("/api/v1/meses/$id") {
			comAutorizacao(TokenTestes.UID_OUTRO)
		}
			.andExpect { status { isNotFound() } }
	}

	@Test
	fun `get por referencia retorna o mes correspondente ou 404`() {
		criarMes(REQUEST_OUTUBRO)

		mockMvc.get("/api/v1/meses") {
			comAutorizacao()
			param("referencia", "2026-10")
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.mesReferencia") { value("2026-10-01") } }

		mockMvc.get("/api/v1/meses") {
			comAutorizacao()
			param("referencia", "2026-11")
		}
			.andExpect { status { isNotFound() } }
	}

	@Test
	fun `get ultimo retorna o mes mais recente do usuario`() {
		criarMes(REQUEST_OUTUBRO)
		criarMes(REQUEST_OUTUBRO.replace("2026-10-01", "2026-11-01"))

		mockMvc.get("/api/v1/meses/ultimo") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.mesReferencia") { value("2026-11-01") } }
	}

	@Test
	fun `get ultimo sem meses retorna 404`() {
		mockMvc.get("/api/v1/meses/ultimo") {
			comAutorizacao()
		}
			.andExpect { status { isNotFound() } }
	}

	@Test
	fun `get por referencia com formato invalido retorna 400 com mensagem clara`() {
		mockMvc.get("/api/v1/meses") {
			comAutorizacao()
			param("referencia", "outubro")
		}
			.andExpect { status { isBadRequest() } }
			.andExpect { jsonPath("$.mensagem") { value("referencia deve estar no formato YYYY-MM: outubro") } }
	}

	@Test
	fun `dinheiro e serializado como string na resposta`() {
		val id = criarMes(REQUEST_OUTUBRO)

		mockMvc.get("/api/v1/meses/$id") {
			comAutorizacao()
		}
			.andExpect { status { isOk() } }
			.andExpect { jsonPath("$.deficit") { isString() } }
			.andExpect { jsonPath("$.deficit") { value("0.00") } }
			.andExpect { jsonPath("$.parametrosCalculo.tetos.essenciais") { isString() } }
			.andExpect { jsonPath("$.parametrosCalculo.tetos.essenciais") { value("1300.00") } }
			.andExpect { jsonPath("$.parametrosCalculo.tetos.lazer") { value("700.00") } }
	}

	@Test
	fun `alternancias ausentes na request viram true`() {
		val request = """
			{
				"mesReferencia": "2026-12-01",
				"parametrosCalculo": {
					"versaoEsquema": 1,
					"percentuais": { "essenciais": 0.50, "lazer": 0.30 },
					"tetos": { "essenciais": "1300.00", "lazer": "700.00" }
				}
			}
		""".trimIndent()

		mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = request
		}
			.andExpect { status { isCreated() } }
			.andExpect { jsonPath("$.alternarInvestir") { value(true) } }
			.andExpect { jsonPath("$.alternarLazer") { value(true) } }
	}

	private fun criarMes(request: String): String {
		val resposta = mockMvc.post("/api/v1/meses") {
			comAutorizacao()
			contentType = MediaType.APPLICATION_JSON
			content = request
		}
			.andExpect { status { isCreated() } }
			.andReturn()

		return objectMapper.readTree(resposta.response.contentAsString)["id"].asText()
	}
}
