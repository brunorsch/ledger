package br.dev.brunorsch.ledger.server.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(MesNaoEncontradoException::class)
	fun tratarMesNaoEncontrado(ex: MesNaoEncontradoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse(ex.message!!))

	@ExceptionHandler(LancamentoNaoEncontradoException::class)
	fun tratarLancamentoNaoEncontrado(ex: LancamentoNaoEncontradoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse(ex.message!!))

	@ExceptionHandler(ValidacaoException::class)
	fun tratarValidacao(ex: ValidacaoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErroResponse(ex.message!!))

	@ExceptionHandler(UsuarioNaoEncontradoException::class)
	fun tratarUsuarioNaoEncontrado(ex: UsuarioNaoEncontradoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErroResponse(ex.message!!))

	data class ErroResponse(val mensagem: String)
}
