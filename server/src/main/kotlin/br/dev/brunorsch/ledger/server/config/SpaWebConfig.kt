package br.dev.brunorsch.ledger.server.config

import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import java.time.Duration

@Configuration
class SpaWebConfig(
	private val webProperties: WebProperties,
) : WebMvcConfigurer {

	private val localizacoesEstaticas: Array<String>
		get() = webProperties.resources.staticLocations

	private val localizacoesAssets: Array<String>
		get() = localizacoesEstaticas.map { "${it}assets/" }.toTypedArray()

	override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
		// O padrão `/assets/**` remove o prefixo `assets/` do caminho antes de resolver,
		// então as localizações já apontam para o subdiretório `assets/`.
		registry.addResourceHandler("/assets/**")
			.addResourceLocations(*localizacoesAssets)
			.setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())

		registry.addResourceHandler("/index.html")
			.addResourceLocations(*localizacoesEstaticas)
			.setCacheControl(CacheControl.noStore())

		// O fallback devolve o index do SPA para rotas do roteador do front, por isso
		// não pode ser cacheado: a próxima versão do index precisa ser sempre buscada.
		registry.addResourceHandler("/**")
			.addResourceLocations(*localizacoesEstaticas)
			.setCacheControl(CacheControl.noStore())
			.resourceChain(true)
			.addResolver(FallbackIndexResourceResolver())
	}

	private class FallbackIndexResourceResolver : PathResourceResolver() {

		override fun getResource(resourcePath: String, location: Resource): Resource? {
			val recursoExistente = super.getResource(resourcePath, location)
			if (recursoExistente != null) {
				return recursoExistente
			}

			if (!deveServirIndex(resourcePath)) {
				return null
			}

			return location.createRelative("index.html").takeIf { it.exists() }
		}

		private fun deveServirIndex(resourcePath: String): Boolean {
			if (resourcePath.startsWith("api/") || resourcePath.startsWith("actuator/")) {
				return false
			}

			return !resourcePath.substringAfterLast('/').contains('.')
		}
	}
}
