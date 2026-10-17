package br.dev.brunorsch.ledger.server.repository

import br.dev.brunorsch.ledger.server.domain.Lancamento
import br.dev.brunorsch.ledger.server.domain.LancamentoRegistro
import org.springframework.data.repository.CrudRepository

/**
 * Repositório dos registros de lançamentos (tabela `lancamentos`).
 *
 * O sumário usa [listarPorMes], que devolve os lançamentos do mês como
 * objetos de domínio a partir da method query derivada.
 */
interface LancamentoRepository : CrudRepository<LancamentoRegistro, Long> {

	fun findAllByMesIdAndUsuarioIdOrderById(mesId: Long, usuarioId: Long): List<LancamentoRegistro>

	/** Lançamentos do mês convertidos para o domínio, em ordem de criação. */
	fun listarPorMes(mesId: Long, usuarioId: Long): List<Lancamento> =
		findAllByMesIdAndUsuarioIdOrderById(mesId, usuarioId).map(LancamentoRegistro::paraDominio)
}
