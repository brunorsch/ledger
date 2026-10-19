package br.dev.brunorsch.ledger.server.exception

import br.dev.brunorsch.ledger.server.config.CorrelationIdFilter
import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class ApiExceptionHandler(
	private val usuarioAutenticadoService: UsuarioAutenticadoService,
) {

	private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

	@ExceptionHandler(MesNaoEncontradoException::class)
	fun tratarMesNaoEncontrado(ex: MesNaoEncontradoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse(ex.message!!))

	@ExceptionHandler(LancamentoNaoEncontradoException::class)
	fun tratarLancamentoNaoEncontrado(ex: LancamentoNaoEncontradoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse(ex.message!!))

	@ExceptionHandler(GastoFixoNaoEncontradoException::class)
	fun tratarGastoFixoNaoEncontrado(ex: GastoFixoNaoEncontradoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse(ex.message!!))

	@ExceptionHandler(MesJaExisteException::class)
	fun tratarMesJaExiste(ex: MesJaExisteException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse(ex.message!!))

	@ExceptionHandler(ValidacaoException::class)
	fun tratarValidacao(ex: ValidacaoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErroResponse(ex.message!!))

	@ExceptionHandler(UsuarioNaoEncontradoException::class)
	fun tratarUsuarioNaoEncontrado(ex: UsuarioNaoEncontradoException): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErroResponse(ex.message!!))

	@ExceptionHandler(NoResourceFoundException::class)
	fun tratarRecursoNaoEncontrado(): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse("Recurso não encontrado"))

	// Estas exceções do framework não implementam `ErrorResponse`, mas o Spring já
	// as trata como 400 pelo resolver padrão; sem este mapeamento o handler genérico
	// abaixo as converteria em 500.
	@ExceptionHandler(HttpMessageNotReadableException::class, TypeMismatchException::class)
	fun tratarRequisicaoInvalida(): ResponseEntity<ErroResponse> =
		ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErroResponse("Requisição inválida"))

	@ExceptionHandler(Exception::class)
	fun tratarErroInterno(ex: Exception): ResponseEntity<ErroInternoResponse> {
		// Exceções do framework que já carregam um status 4xx (ex.: argumento inválido,
		// método não suportado) seguem o tratamento padrão do Spring em vez de virar 500.
		if (ex is ErrorResponse && ex.statusCode.is4xxClientError) {
			throw ex
		}

		registrarUsuarioNoMdc()
		log.error("Erro interno ao processar requisição", ex)
		val correlationId = MDC.get(CorrelationIdFilter.CHAVE_CORRELATION_ID) ?: ""
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ErroInternoResponse(mensagem = "Erro interno", correlationId = correlationId))
	}

	private fun registrarUsuarioNoMdc() {
		val authentication = SecurityContextHolder.getContext().authentication ?: return
		try {
			usuarioAutenticadoService.resolver(authentication)
		} catch (_: Exception) {
			// A resolução do usuário não pode alterar o erro original.
		}
	}

	data class ErroResponse(val mensagem: String)
	data class ErroInternoResponse(val mensagem: String, val correlationId: String)
}
