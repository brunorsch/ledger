package br.dev.brunorsch.ledger.server.config

import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoLocal
import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Autenticação do perfil local: resolve o usuário pelo header `X-Usuario-Id`
 * (ou pelo seed quando ausente) e popula o `SecurityContext`.
 */
class FiltroAutenticacaoLocal(
	private val usuarioAutenticadoService: UsuarioAutenticadoService,
) : OncePerRequestFilter() {

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		val usuarioIdDoHeader = request.getHeader(UsuarioAutenticadoService.HEADER_USUARIO_ID)?.toLongOrNull()
		val usuarioId = usuarioAutenticadoService.resolverLocal(usuarioIdDoHeader)
		val authentication = UsernamePasswordAuthenticationToken(
			UsuarioAutenticadoLocal(usuarioId),
			null,
			listOf(SimpleGrantedAuthority(AUTORIDADE)),
		)

		val context = SecurityContextHolder.createEmptyContext()
		context.authentication = authentication
		SecurityContextHolder.setContext(context)

		filterChain.doFilter(request, response)
	}

	private companion object {
		const val AUTORIDADE = "ROLE_USUARIO"
	}
}
