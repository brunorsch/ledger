package br.dev.brunorsch.ledger.server.controller

import br.dev.brunorsch.ledger.server.dto.AlterarLancamentoRequest
import br.dev.brunorsch.ledger.server.dto.CriarLancamentoRequest
import br.dev.brunorsch.ledger.server.dto.LancamentoResponse
import br.dev.brunorsch.ledger.server.service.LancamentoService
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
class LancamentoController(
	private val lancamentoService: LancamentoService,
	private val usuarioAutenticadoService: UsuarioAutenticadoService,
) {

	@PostMapping("/meses/{mesId}/lancamentos")
	fun criar(
		@PathVariable mesId: UUID,
		@RequestBody request: CriarLancamentoRequest,
		authentication: Authentication,
	): ResponseEntity<LancamentoResponse> {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		val lancamento = lancamentoService.criar(mesId, usuarioId, request)
		return ResponseEntity.status(HttpStatus.CREATED).body(LancamentoResponse.fromDomain(lancamento))
	}

	@GetMapping("/meses/{mesId}/lancamentos")
	fun listar(
		@PathVariable mesId: UUID,
		authentication: Authentication,
	): List<LancamentoResponse> {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return lancamentoService.listar(mesId, usuarioId).map(LancamentoResponse::fromDomain)
	}

	@PatchMapping("/lancamentos/{id}")
	fun alterar(
		@PathVariable id: UUID,
		@RequestBody request: AlterarLancamentoRequest,
		authentication: Authentication,
	): LancamentoResponse {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		return LancamentoResponse.fromDomain(lancamentoService.alterar(id, usuarioId, request))
	}

	@DeleteMapping("/lancamentos/{id}")
	fun remover(
		@PathVariable id: UUID,
		authentication: Authentication,
	): ResponseEntity<Void> {
		val usuarioId = usuarioAutenticadoService.resolver(authentication)
		lancamentoService.remover(id, usuarioId)
		return ResponseEntity.noContent().build()
	}
}
