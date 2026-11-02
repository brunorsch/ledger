package br.dev.brunorsch.ledger.server

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.client.RestTestClient
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

/**
 * Usa um container próprio, e não o compartilhado de [SuportePostgres], para que a
 * requisição do teste seja o primeiro login de um banco recém-migrado,
 * independentemente da ordem de execução das outras classes.
 */
@Testcontainers
@Import(SegurancaTesteConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PrimeiroLoginApiTest {

	@LocalServerPort
	var porta: Int = 0

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	companion object {

		@JvmStatic
		@ServiceConnection
		val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:17").apply { start() }
	}

	@Test
	fun `primeiro login em banco recem-migrado cria usuario sem colidir com o seed`() {
		val cliente = RestTestClient.bindToServer().baseUrl("http://localhost:$porta").build()

		cliente.get().uri("/api/v1/gastos-fixos")
			.header("Authorization", TokenTestes.autorizacao("teste-primeiro-login"))
			.exchange()
			.expectStatus().isOk()

		assertThat(TokenTestes.usuarioId(jdbcTemplate, "teste-primeiro-login")).isNotEqualTo(1L)
	}
}
