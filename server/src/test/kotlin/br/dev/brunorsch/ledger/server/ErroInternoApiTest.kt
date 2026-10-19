package br.dev.brunorsch.ledger.server

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension::class)
class ErroInternoApiTest : SuportePostgres() {

	@LocalServerPort
	var porta: Int = 0

	@Test
	fun `excecao nao tratada vira 500 com o correlation id enviado`() {
		RestTestClient.bindToServer()
			.baseUrl("http://localhost:$porta")
			.build()
			.get()
			.uri("/api/v1/teste-erro")
			.header("Authorization", TokenTestes.autorizacao())
			.header("X-Correlation-Id", "teste-correlacao-123")
			.exchange()
			.expectStatus().isEqualTo(500)
			.expectHeader().valueEquals("X-Correlation-Id", "teste-correlacao-123")
			.expectBody().jsonPath("$.correlationId").isEqualTo("teste-correlacao-123")
	}

	@Test
	fun `log estruturado registra a falha com correlacao, usuario e trace`(output: CapturedOutput) {
		RestTestClient.bindToServer()
			.baseUrl("http://localhost:$porta")
			.build()
			.get()
			.uri("/api/v1/teste-erro")
			.header("Authorization", TokenTestes.autorizacao())
			.header("X-Correlation-Id", "teste-correlacao-123")
			.exchange()
			.expectStatus().isEqualTo(500)

		assertThat(output).contains("\"log\":{\"level\":\"ERROR\"")
		assertThat(output).contains("\"message\":\"Erro interno ao processar requisição\"")
		assertThat(output).contains("\"error\":{\"type\":\"java.lang.IllegalStateException\"")
		assertThat(output).contains("falha simulada")
		assertThat(output).contains("\"correlationId\":\"teste-correlacao-123\"")
		assertThat(output).containsPattern("\"usuarioId\":\"\\d+\"")
		assertThat(output).containsPattern("\"traceId\":\"[0-9a-f]+\"")
		assertThat(output).containsPattern("\"spanId\":\"[0-9a-f]+\"")
		assertThat(output).containsPattern("\"@timestamp\":\"[^\"]+\"")
	}

	@Test
	fun `resposta gera correlation id quando o header nao vem`() {
		RestTestClient.bindToServer()
			.baseUrl("http://localhost:$porta")
			.build()
			.get()
			.uri("/api/v1/ping")
			.header("Authorization", TokenTestes.autorizacao())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().valueMatches(
				"X-Correlation-Id",
				"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
			)
	}

	@TestConfiguration
	class ConfiguracaoDeTeste {

		@Bean
		fun controladorDeErroDeTeste(): ControladorDeErroDeTeste = ControladorDeErroDeTeste()
	}

	@RestController
	@RequestMapping("/api/v1")
	class ControladorDeErroDeTeste {

		@GetMapping("/teste-erro")
		fun erro(): String = throw IllegalStateException("falha simulada")
	}
}
