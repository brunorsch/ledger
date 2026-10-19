package br.dev.brunorsch.ledger.server.config

import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

/**
 * Cadeia de segurança do perfil local: dispensa token e usa o header
 * `X-Usuario-Id`, preservando o fluxo de desenvolvimento sem Firebase.
 */
@Configuration
@Profile("local")
class SegurancaLocalConfig {

	@Bean
	@Order(1)
	fun cadeiaLocal(
		http: HttpSecurity,
		usuarioAutenticadoService: UsuarioAutenticadoService,
	): SecurityFilterChain {
		http {
			securityMatcher("/api/**")
			csrf { disable() }
			httpBasic { disable() }
			formLogin { disable() }
			authorizeHttpRequests {
				authorize(anyRequest, authenticated)
			}
			addFilterBefore<BearerTokenAuthenticationFilter>(
				FiltroAutenticacaoLocal(usuarioAutenticadoService),
			)
		}
		return http.build()
	}
}
