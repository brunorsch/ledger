package br.dev.brunorsch.ledger.server.controller

import br.dev.brunorsch.ledger.server.dto.AlterarAlternanciasRequest
import br.dev.brunorsch.ledger.server.dto.AlternanciasResponse
import br.dev.brunorsch.ledger.server.dto.CriarMesRequest
import br.dev.brunorsch.ledger.server.dto.MesResponse
import br.dev.brunorsch.ledger.server.dto.MesSumarioResponse
import br.dev.brunorsch.ledger.server.service.MesService
import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/meses")
class MesController(
	private val mesService: MesService,
	private val usuarioAutenticadoService: UsuarioAutenticadoService,
) {

	@PostMapping
	fun criar(
		@RequestBody request: CriarMesRequest,
		authentication: Authentication,
	): ResponseEntity<MesResponse> {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		val mes = mesService.criar(request, usuarioId)
		return ResponseEntity.status(HttpStatus.CREATED).body(MesResponse.fromDomain(mes))
	}

	@GetMapping("/{id}")
	fun obterPorId(
		@PathVariable id: UUID,
		authentication: Authentication,
	): MesResponse {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return MesResponse.fromDomain(mesService.obterPorIdPublico(id, usuarioId))
	}

	@GetMapping("/ultimo")
	fun obterUltimo(authentication: Authentication): MesResponse {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return MesResponse.fromDomain(mesService.obterUltimo(usuarioId))
	}

	@GetMapping
	fun obterPorReferencia(
		@RequestParam("referencia") referencia: String,
		authentication: Authentication,
	): MesResponse {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return MesResponse.fromDomain(mesService.obterPorReferencia(referencia, usuarioId))
	}

	@GetMapping("/{id}/sumario")
	fun obterSumario(
		@PathVariable id: UUID,
		authentication: Authentication,
	): MesSumarioResponse {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return mesService.obterSumario(id, usuarioId)
	}

	@PatchMapping("/{id}/alternancias")
	fun alterarAlternancias(
		@PathVariable id: UUID,
		@RequestBody request: AlterarAlternanciasRequest,
		authentication: Authentication,
	): AlternanciasResponse {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return mesService.alterarAlternancias(id, usuarioId, request)
	}
}
