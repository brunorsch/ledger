package br.dev.brunorsch.ledger.server

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
@Import(SegurancaTesteConfig::class)
abstract class SuportePostgres {

	companion object {

		@JvmStatic
		@ServiceConnection
		val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:17").apply { start() }
	}
}
