package br.dev.brunorsch.ledger.server.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Propaga um identificador de correlação por requisição.
 *
 * O valor vem do header `X-Correlation-Id`; quando ausente, um UUID é gerado.
 * O identificador entra no MDC para aparecer nos logs estruturados; o usuário
 * autenticado é publicado pelo [br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService].
 */
@Component
class CorrelationIdFilter : OncePerRequestFilter() {

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		val correlationId = request.getHeader(HEADER_CORRELATION_ID)
			?.takeIf { it.isNotBlank() }
			?: UUID.randomUUID().toString()

		MDC.put(CHAVE_CORRELATION_ID, correlationId)
		response.setHeader(HEADER_CORRELATION_ID, correlationId)

		try {
			filterChain.doFilter(request, response)
		} finally {
			MDC.remove(CHAVE_CORRELATION_ID)
			MDC.remove(CHAVE_USUARIO_ID)
		}
	}

	companion object {
		const val HEADER_CORRELATION_ID = "X-Correlation-Id"
		const val CHAVE_CORRELATION_ID = "correlationId"
		const val CHAVE_USUARIO_ID = "usuarioId"
	}
}
