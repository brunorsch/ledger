package br.dev.brunorsch.ledger.server.controller

import br.dev.brunorsch.ledger.server.dto.AlterarGastoFixoRequest
import br.dev.brunorsch.ledger.server.dto.CriarGastoFixoRequest
import br.dev.brunorsch.ledger.server.dto.GastoFixoResponse
import br.dev.brunorsch.ledger.server.service.GastoFixoService
import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class GastoFixoController(
	private val gastoFixoService: GastoFixoService,
	private val usuarioAutenticadoService: UsuarioAutenticadoService,
) {

	@PostMapping("/gastos-fixos")
	fun criar(
		@RequestBody request: CriarGastoFixoRequest,
		authentication: Authentication,
	): ResponseEntity<GastoFixoResponse> {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		val gastoFixo = gastoFixoService.criar(usuarioId, request)
		return ResponseEntity.status(HttpStatus.CREATED).body(GastoFixoResponse.fromDomain(gastoFixo))
	}

	@GetMapping("/gastos-fixos")
	fun listar(authentication: Authentication): List<GastoFixoResponse> {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return gastoFixoService.listar(usuarioId).map(GastoFixoResponse::fromDomain)
	}

	@PatchMapping("/gastos-fixos/{id}")
	fun alterar(
		@PathVariable id: UUID,
		@RequestBody request: AlterarGastoFixoRequest,
		authentication: Authentication,
	): GastoFixoResponse {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return GastoFixoResponse.fromDomain(gastoFixoService.alterar(id, usuarioId, request))
	}

	@DeleteMapping("/gastos-fixos/{id}")
	fun remover(
		@PathVariable id: UUID,
		authentication: Authentication,
	): ResponseEntity<Void> {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		gastoFixoService.remover(id, usuarioId)
		return ResponseEntity.noContent().build()
	}
}
