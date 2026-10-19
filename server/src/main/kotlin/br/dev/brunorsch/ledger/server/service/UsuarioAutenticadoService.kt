package br.dev.brunorsch.ledger.server.service

import br.dev.brunorsch.ledger.server.config.CorrelationIdFilter
import br.dev.brunorsch.ledger.server.exception.UsuarioNaoEncontradoException
import org.slf4j.MDC
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

/**
 * Resolve o id interno do usuário autenticado.
 *
 * No ambiente de produção o usuário vem do ID token do Firebase (`sub`); no
 * perfil local, do header `X-Usuario-Id` (seed quando ausente). O id resolvido
 * é publicado no MDC para os logs estruturados.
 */
@Service
class UsuarioAutenticadoService(private val jdbcTemplate: JdbcTemplate) {

	fun resolver(authentication: Authentication): Long {
		val usuarioId = usuarioIdDe(authentication)
		MDC.put(CorrelationIdFilter.CHAVE_USUARIO_ID, usuarioId.toString())
		return usuarioId
	}

	private fun usuarioIdDe(authentication: Authentication): Long =
		when (val principal = authentication.principal) {
			is UsuarioAutenticadoLocal -> principal.usuarioId
			is Jwt -> resolverPorJwt(principal)
			else -> throw UsuarioNaoEncontradoException(ID_USUARIO_SEED)
		}

	/**
	 * Garante a existência do usuário do token e devolve o id interno.
	 *
	 * O `sub` é o uid estável do usuário; o primeiro login grava o provedor e os
	 * logins seguintes o preservam.
	 */
	private fun resolverPorJwt(jwt: Jwt): Long {
		val uid = jwt.subject
		jdbcTemplate.update(
			"INSERT INTO usuarios (sujeito_provedor, identity_provider) VALUES (?, ?) ON CONFLICT (sujeito_provedor) DO NOTHING",
			uid,
			provedor(jwt),
		)
		return jdbcTemplate.queryForObject(
			"SELECT id FROM usuarios WHERE sujeito_provedor = ?",
			Long::class.java,
			uid,
		)!!
	}

	/**
	 * O Firebase aninha o provedor em `firebase.sign_in_provider`; o claim plano é
	 * aceito como fallback.
	 */
	private fun provedor(jwt: Jwt): String? {
		val firebase = jwt.claims["firebase"] as? Map<*, *>
		return (firebase?.get("sign_in_provider") as? String)
			?: jwt.getClaimAsString("firebase.sign_in_provider")
	}

	fun resolverLocal(usuarioIdDoHeader: Long?): Long {
		val usuarioId = usuarioIdDoHeader ?: ID_USUARIO_SEED
		val existe = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM usuarios WHERE id = ?",
			Long::class.java,
			usuarioId,
		) == 1L
		if (!existe) {
			throw UsuarioNaoEncontradoException(usuarioId)
		}
		return usuarioId
	}

	companion object {
		const val HEADER_USUARIO_ID = "X-Usuario-Id"

		private const val ID_USUARIO_SEED = 1L
	}
}
