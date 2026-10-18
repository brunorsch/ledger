package br.dev.brunorsch.ledger.server.config

import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
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
 * O identificador e o usuário autenticado (header `X-Usuario-Id`) entram no MDC
 * para que apareçam nos logs estruturados.
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

		request.getHeader(UsuarioAutenticadoService.HEADER_USUARIO_ID)?.let { MDC.put(CHAVE_USUARIO_ID, it) }

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
