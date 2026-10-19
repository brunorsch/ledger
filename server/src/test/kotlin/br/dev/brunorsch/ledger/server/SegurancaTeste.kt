package br.dev.brunorsch.ledger.server

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtAudienceValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Substitui o decoder de produção por um de chave simétrica (`HS256`), para que
 * os testes assinem tokens localmente sem depender do Firebase.
 */
@TestConfiguration
class SegurancaTesteConfig {

	@Bean
	@Primary
	fun jwtDecoder(): JwtDecoder {
		val decoder = NimbusJwtDecoder.withSecretKey(CHAVE)
			.macAlgorithm(MacAlgorithm.HS256)
			.build()
		decoder.setJwtValidator(
			JwtValidators.createDefaultWithValidators(
				JwtIssuerValidator(ISSUER),
				JwtAudienceValidator(AUDIENCE),
			),
		)
		return decoder
	}

	companion object {
		const val ISSUER = "https://securetoken.google.com/teste-projeto"
		const val AUDIENCE = "teste-projeto"

		private const val SEGREDO = "segredo-de-teste-token-fake-com-32-bytes!!"

		val CHAVE: SecretKey = SecretKeySpec(SEGREDO.toByteArray(), "HmacSHA256")
	}
}

/** Gera tokens compatíveis com [SegurancaTesteConfig] e apoia a asserção de usuários. */
object TokenTestes {

	const val UID_PADRAO = "teste-usuario-1"
	const val UID_OUTRO = "teste-usuario-2"

	fun autorizacao(uid: String = UID_PADRAO): String = "Bearer ${gerar(uid)}"

	fun gerar(
		uid: String,
		provedor: String = "google.com",
		expirado: Boolean = false,
		audiencia: String = SegurancaTesteConfig.AUDIENCE,
		issuer: String = SegurancaTesteConfig.ISSUER,
		chave: SecretKey = SegurancaTesteConfig.CHAVE,
	): String {
		val agora = Instant.now()
		val expiracao = if (expirado) agora.minusSeconds(3600) else agora.plusSeconds(3600)
		val reivindicacoes = JWTClaimsSet.Builder()
			.issuer(issuer)
			.audience(audiencia)
			.subject(uid)
			.issueTime(Date.from(expiracao.minusSeconds(3600)))
			.expirationTime(Date.from(expiracao))
			.claim("firebase", mapOf("sign_in_provider" to provedor))
			.build()
		val header = JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build()

		val token = SignedJWT(header, reivindicacoes)
		token.sign(MACSigner(chave))
		return token.serialize()
	}

	fun usuarioId(jdbcTemplate: JdbcTemplate, uid: String): Long =
		jdbcTemplate.queryForObject(
			"SELECT id FROM usuarios WHERE sujeito_provedor = ?",
			Long::class.java,
			uid,
		)!!
}

fun MockHttpServletRequestDsl.comAutorizacao(uid: String = TokenTestes.UID_PADRAO) {
	header(HttpHeaders.AUTHORIZATION, TokenTestes.autorizacao(uid))
}
