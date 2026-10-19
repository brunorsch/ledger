package br.dev.brunorsch.ledger.server

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SegurancaApiTest : SuportePostgres() {

	@LocalServerPort
	var porta: Int = 0

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	companion object {
		private val OUTRA_CHAVE: SecretKey =
			SecretKeySpec("outro-segredo-diferente-com-32-bytes!!".toByteArray(), "HmacSHA256")
	}

	// AC #6: token ausente ou inválido (expirado, audience ou assinatura erradas) retorna 401.
	@Test
	fun `api exige token valido`() {
		cliente().get().uri("/api/v1/meses")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isUnauthorized()

		cliente().get().uri("/api/v1/meses")
			.header("Authorization", "Bearer ${TokenTestes.gerar("teste-expirado", expirado = true)}")
			.exchange()
			.expectStatus().isUnauthorized()

		cliente().get().uri("/api/v1/meses")
			.header("Authorization", "Bearer ${TokenTestes.gerar("teste-audiencia", audiencia = "outro-projeto")}")
			.exchange()
			.expectStatus().isUnauthorized()

		cliente().get().uri("/api/v1/meses")
			.header("Authorization", "Bearer ${TokenTestes.gerar("teste-assinatura", chave = OUTRA_CHAVE)}")
			.exchange()
			.expectStatus().isUnauthorized()
	}

	// AC #3: o primeiro request de um uid novo cria o usuário com o provedor do token.
	@Test
	fun `primeiro request cria usuario com o provedor do token`() {
		cliente().get().uri("/api/v1/gastos-fixos")
			.header("Authorization", TokenTestes.autorizacao("teste-novo-uid"))
			.exchange()
			.expectStatus().isOk()

		val provedor = jdbcTemplate.queryForObject(
			"SELECT identity_provider FROM usuarios WHERE sujeito_provedor = ?",
			String::class.java,
			"teste-novo-uid",
		)

		assertThat(provedor).isEqualTo("google.com")
	}

	// AC #4: o provedor do primeiro login é preservado nos logins seguintes.
	@Test
	fun `provedor do primeiro login e preservado nos logins seguintes`() {
		cliente().get().uri("/api/v1/gastos-fixos")
			.header("Authorization", TokenTestes.autorizacao("teste-provedor-unico"))
			.exchange()
			.expectStatus().isOk()
		val idPrimeiroLogin = TokenTestes.usuarioId(jdbcTemplate, "teste-provedor-unico")

		cliente().get().uri("/api/v1/gastos-fixos")
			.header(
				"Authorization",
				"Bearer ${TokenTestes.gerar("teste-provedor-unico", provedor = "facebook.com")}",
			)
			.exchange()
			.expectStatus().isOk()

		val idSegundoLogin = TokenTestes.usuarioId(jdbcTemplate, "teste-provedor-unico")
		val provedor = jdbcTemplate.queryForObject(
			"SELECT identity_provider FROM usuarios WHERE sujeito_provedor = ?",
			String::class.java,
			"teste-provedor-unico",
		)

		assertThat(idSegundoLogin).isEqualTo(idPrimeiroLogin)
		assertThat(provedor).isEqualTo("google.com")
	}

	// AC #7: cada usuário só acessa os próprios meses (isolamento por usuario_id).
	@Test
	fun `usuario nao acessa mes de outro`() {
		criarMes("teste-usuario-a", "2026-10-01")
		criarMes("teste-usuario-b", "2026-11-01")
		val mesA = mesIdPublico("teste-usuario-a")
		val mesB = mesIdPublico("teste-usuario-b")

		cliente().get().uri("/api/v1/meses/$mesB")
			.header("Authorization", TokenTestes.autorizacao("teste-usuario-a"))
			.exchange()
			.expectStatus().isNotFound()

		cliente().patch().uri("/api/v1/meses/$mesB/alternancias")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""{"alternarInvestir": false}""")
			.header("Authorization", TokenTestes.autorizacao("teste-usuario-a"))
			.exchange()
			.expectStatus().isNotFound()

		cliente().get().uri("/api/v1/meses/$mesA")
			.header("Authorization", TokenTestes.autorizacao("teste-usuario-b"))
			.exchange()
			.expectStatus().isNotFound()
	}

	// AC #8: o health check segue público.
	@Test
	fun `health e publico`() {
		cliente().get().uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
	}

	private fun cliente(): RestTestClient = RestTestClient.bindToServer()
		.baseUrl("http://localhost:$porta")
		.build()

	private fun criarMes(uid: String, referencia: String) {
		cliente().post().uri("/api/v1/meses")
			.contentType(MediaType.APPLICATION_JSON)
			.body(
				"""
				{
					"mesReferencia": "$referencia",
					"parametrosCalculo": {
						"versaoEsquema": 1,
						"percentuais": { "essenciais": 0.50, "lazer": 0.30 },
						"tetos": { "essenciais": "1300.00", "lazer": "700.00" }
					}
				}
				""".trimIndent(),
			)
			.header("Authorization", TokenTestes.autorizacao(uid))
			.exchange()
			.expectStatus().isCreated()
	}

	private fun mesIdPublico(uid: String): UUID = jdbcTemplate.queryForObject(
		"SELECT id_publico FROM meses WHERE usuario_id = (SELECT id FROM usuarios WHERE sujeito_provedor = ?)",
		UUID::class.java,
		uid,
	)!!
}
