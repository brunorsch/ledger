package br.dev.brunorsch.ledger.server

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.get

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = ["spring.web.resources.static-locations=file:src/test/resources/static-fixture/"],
)
@AutoConfigureMockMvc
class EmpacotamentoWebTest : SuportePostgres() {

	@Autowired
	lateinit var mockMvc: MockMvc

	@LocalServerPort
	var porta: Int = 0

	@Test
	fun `rota do spa sem extensao devolve o index html`() {
		mockMvc.get("/meses/2026-11")
			.andExpect { status { isOk() } }
			.andExpect { content { contentTypeCompatibleWith(MediaType.TEXT_HTML) } }
			.andExpect { content { string(containsString("""<div id="root">""")) } }
	}

	@Test
	fun `index html e servido sem cache`() {
		mockMvc.get("/index.html")
			.andExpect { status { isOk() } }
			.andExpect { header { string("Cache-Control", containsString("no-store")) } }
	}

	@Test
	fun `assets com hash sao servidos como imutaveis`() {
		mockMvc.get("/assets/app.a1b2c3.js")
			.andExpect { status { isOk() } }
			.andExpect { header { string("Cache-Control", containsString("max-age=31536000")) } }
			.andExpect { header { string("Cache-Control", containsString("immutable")) } }
	}

	@Test
	fun `rota de api inexistente continua retornando 404 em json`() {
		// O MockMvc nao dispara o error dispatch do Boot, entao validamos o 404 JSON
		// contra o servidor real, que e onde o BasicErrorController responde.
		RestTestClient.bindToServer()
			.baseUrl("http://localhost:$porta")
			.build()
			.get()
			.uri("/api/v1/nao-existe")
			.header("Authorization", TokenTestes.autorizacao())
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
	}
}
