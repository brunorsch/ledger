package br.dev.brunorsch.ledger.server

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class SuportePostgres {

	companion object {

		@JvmStatic
		@ServiceConnection
		val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:17").apply { start() }
	}
}