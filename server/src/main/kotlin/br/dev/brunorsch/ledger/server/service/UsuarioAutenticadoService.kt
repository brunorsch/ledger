package br.dev.brunorsch.ledger.server.service

import br.dev.brunorsch.ledger.server.exception.UsuarioNaoEncontradoException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

/**
 * Resolve o id interno do usuário autenticado.
 *
 * Enquanto a integração OAuth não entra (task dedicada), o usuário vem do
 * header `X-Usuario-Id`; quando o header está ausente, vale o usuário seed
 * único do MVP. A existência no banco é sempre verificada.
 */
@Service
class UsuarioAutenticadoService(private val jdbcTemplate: JdbcTemplate) {

	fun resolver(usuarioIdDoHeader: Long?): Long {
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
