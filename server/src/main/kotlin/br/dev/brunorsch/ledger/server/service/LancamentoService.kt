package br.dev.brunorsch.ledger.server.service

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.Lancamento
import br.dev.brunorsch.ledger.server.domain.TipoLancamento
import br.dev.brunorsch.ledger.server.dto.AlterarLancamentoRequest
import br.dev.brunorsch.ledger.server.dto.CriarLancamentoRequest
import br.dev.brunorsch.ledger.server.exception.LancamentoNaoEncontradoException
import br.dev.brunorsch.ledger.server.exception.MesNaoEncontradoException
import br.dev.brunorsch.ledger.server.exception.ValidacaoException
import br.dev.brunorsch.ledger.server.repository.LancamentoRepository
import br.dev.brunorsch.ledger.server.repository.MesRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class LancamentoService(
	private val lancamentoRepository: LancamentoRepository,
	private val mesRepository: MesRepository,
) {

	fun criar(mesIdPublico: UUID, usuarioId: Long, request: CriarLancamentoRequest): Lancamento {
		val mes = mesRepository.findByIdPublicoAndUsuarioId(mesIdPublico, usuarioId)
			?: throw MesNaoEncontradoException()

		validarValor(request.valor)

		val lancamento = when (request.tipo) {
			TipoLancamento.DESPESA -> Lancamento.Despesa(
				valor = request.valor,
				pago = false,
				descricao = request.descricao,
			).atualizarPago(request.pago ?: false, request.pagoEm, Instant.now())

			TipoLancamento.RECEITA -> Lancamento.Receita(
				valor = request.valor,
				descricao = request.descricao,
			)
		}
		return lancamentoRepository.criar(mes.id!!, usuarioId, lancamento)
	}

	fun listar(mesIdPublico: UUID, usuarioId: Long): List<Lancamento> {
		val mes = mesRepository.findByIdPublicoAndUsuarioId(mesIdPublico, usuarioId)
			?: throw MesNaoEncontradoException()
		return lancamentoRepository.listarPorMes(mes.id!!, usuarioId)
	}

	fun alterar(idPublico: UUID, usuarioId: Long, request: AlterarLancamentoRequest): Lancamento {
		val atual = lancamentoRepository.buscarPorIdPublicoEUsuario(idPublico, usuarioId)
			?: throw LancamentoNaoEncontradoException()

		val valor = request.valor ?: atual.valor
		validarValor(valor)

		val atualizado = when (atual) {
			is Lancamento.Despesa -> atual.copy(
				descricao = request.descricao ?: atual.descricao,
				valor = valor,
			).atualizarPago(request.pago ?: atual.pago, request.pagoEm, Instant.now())

			is Lancamento.Receita -> atual.copy(
				descricao = request.descricao ?: atual.descricao,
				valor = valor,
			)
		}
		lancamentoRepository.atualizar(atualizado, usuarioId)
		return atualizado
	}

	fun remover(idPublico: UUID, usuarioId: Long) {
		if (!lancamentoRepository.removerPorIdPublicoEUsuario(idPublico, usuarioId)) {
			throw LancamentoNaoEncontradoException()
		}
	}

	private fun validarValor(valor: Dinheiro) {
		try {
			Lancamento.validarValorMaiorQueZero(valor)
		} catch (ex: IllegalArgumentException) {
			throw ValidacaoException(ex.message!!)
		}
	}
}
