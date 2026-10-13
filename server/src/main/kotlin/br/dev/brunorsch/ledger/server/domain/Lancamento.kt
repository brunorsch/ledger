package br.dev.brunorsch.ledger.server.domain

/**
 * Lançamento de um mês: receita (entrada) ou despesa (saída) com valor monetário.
 *
 * Use [Lancamento.Receita] para entradas e [Lancamento.Despesa] para saídas.
 * O flag `pago` só faz sentido em [Lancamento.Despesa] — por isso a separação
 * em subtipos data class, em vez de um único tipo com campo nullable.
 */
sealed class Lancamento {
	abstract val valor: Dinheiro
	abstract val descricao: String

	/** Saída de dinheiro do mês. [pago] indica se a despesa já foi quitada. */
	data class Despesa(
		override val valor: Dinheiro,
		val pago: Boolean,
		override val descricao: String,
	) : Lancamento()

	/** Entrada de dinheiro no mês. */
	data class Receita(
		override val valor: Dinheiro,
		override val descricao: String,
	) : Lancamento()
}
