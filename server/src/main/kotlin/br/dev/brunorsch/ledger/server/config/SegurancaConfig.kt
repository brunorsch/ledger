package br.dev.brunorsch.ledger.server.config

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher

/**
 * Cadeia de segurança do ambiente de produção, autenticada por JWT do Firebase.
 *
 * A cadeia só exige token sob as rotas de API; o front (SPA) e os assets seguem
 * públicos. O entry point é fixado no bearer para garantir 401 com
 * `WWW-Authenticate: Bearer` mesmo quando o cliente não envia `Accept`.
 */
@Configuration
class SegurancaConfig {

	@Bean
	@Order(2)
	fun cadeiaDeSeguranca(http: HttpSecurity): SecurityFilterChain {
		http {
			csrf { disable() }
			httpBasic { disable() }
			formLogin { disable() }
			authorizeHttpRequests {
				authorize(DispatcherTypeRequestMatcher(DispatcherType.ERROR), permitAll)
				authorize(DispatcherTypeRequestMatcher(DispatcherType.FORWARD), permitAll)
				authorize("/actuator/health", permitAll)
				authorize("/api/**", authenticated)
				authorize(anyRequest, permitAll)
			}
			oauth2ResourceServer {
				jwt { }
			}
			exceptionHandling {
				authenticationEntryPoint = BearerTokenAuthenticationEntryPoint()
			}
		}
		return http.build()
	}
}
