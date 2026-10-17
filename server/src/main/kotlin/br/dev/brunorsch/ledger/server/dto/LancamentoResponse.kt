package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.Lancamento
import br.dev.brunorsch.ledger.server.domain.TipoLancamento
import java.time.Instant
import java.util.UUID

/**
 * Lançamento exposto pela API: [id] é o id público (UUID), nunca o id interno.
 *
 * Receitas não têm `pago` nem `pagoEm` — os campos só existem em
 * [LancamentoResponse.Despesa].
 */
sealed interface LancamentoResponse {
	val id: UUID
	val tipo: TipoLancamento
	val descricao: String
	val valor: Dinheiro

	data class Despesa(
		override val id: UUID,
		override val descricao: String,
		override val valor: Dinheiro,
		val pago: Boolean,
		val pagoEm: Instant?,
	) : LancamentoResponse {
		override val tipo: TipoLancamento get() = TipoLancamento.DESPESA
	}

	data class Receita(
		override val id: UUID,
		override val descricao: String,
		override val valor: Dinheiro,
	) : LancamentoResponse {
		override val tipo: TipoLancamento get() = TipoLancamento.RECEITA
	}

	companion object {
		fun fromDomain(lancamento: Lancamento): LancamentoResponse = when (lancamento) {
			is Lancamento.Despesa -> Despesa(
				id = lancamento.idPublico,
				descricao = lancamento.descricao,
				valor = lancamento.valor,
				pago = lancamento.pago,
				pagoEm = lancamento.pagoEm,
			)
			is Lancamento.Receita -> Receita(
				id = lancamento.idPublico,
				descricao = lancamento.descricao,
				valor = lancamento.valor,
			)
		}
	}
}
