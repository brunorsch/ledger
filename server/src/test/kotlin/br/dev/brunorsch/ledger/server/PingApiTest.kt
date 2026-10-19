package br.dev.brunorsch.ledger.server

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PingApiTest : SuportePostgres() {

	@LocalServerPort
	var porta: Int = 0

	@Test
	fun `ping retorna ok`() {
		RestTestClient.bindToServer()
			.baseUrl("http://localhost:$porta")
			.build()
			.get()
			.uri("/api/v1/ping")
			.header("Authorization", TokenTestes.autorizacao())
			.exchange()
			.expectStatus().isOk()
			.expectBody().jsonPath("$.status").isEqualTo("ok")
	}
}
