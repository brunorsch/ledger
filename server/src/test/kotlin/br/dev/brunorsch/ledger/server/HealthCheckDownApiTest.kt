package br.dev.brunorsch.ledger.server

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.test.web.servlet.client.RestTestClient
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.net.http.HttpClient
import java.time.Duration

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = [
		"spring.datasource.hikari.connection-timeout=1000",
		"spring.datasource.hikari.validation-timeout=1000",
	],
)
@Testcontainers
class HealthCheckDownApiTest {

	@LocalServerPort
	var porta: Int = 0

	@Test
	fun `health fica down quando o banco para`() {
		val client = RestTestClient.bindToServer(requestFactory()).baseUrl("http://localhost:$porta").build()

		client.get().uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody().jsonPath("$.status").isEqualTo("UP")

		postgres.stop()

		client.get().uri("/actuator/health")
			.exchange()
			.expectStatus().isEqualTo(503)
			.expectBody().jsonPath("$.status").isEqualTo("DOWN")
	}

	private fun requestFactory(): JdkClientHttpRequestFactory {
		val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
		return JdkClientHttpRequestFactory(httpClient).apply { setReadTimeout(Duration.ofSeconds(30)) }
	}

	companion object {

		@JvmStatic
		@ServiceConnection
		val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:17").apply { start() }

		@JvmStatic
		@AfterAll
		fun reiniciarBanco() {
			if (!postgres.isRunning) {
				postgres.start()
			}
		}
	}
}
