package br.dev.brunorsch.ledger.server

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthCheckApiTest : SuportePostgres() {

	@LocalServerPort
	var porta: Int = 0

	@Test
	fun `health retorna up com o banco disponivel`() {
		RestTestClient.bindToServer()
			.baseUrl("http://localhost:$porta")
			.build()
			.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody().jsonPath("$.status").isEqualTo("UP")
	}
}
