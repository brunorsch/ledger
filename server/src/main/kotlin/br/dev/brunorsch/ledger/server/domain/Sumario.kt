package br.dev.brunorsch.ledger.server.domain

/**
 * Resultado do cálculo do sumário de um mês.
 *
 * @property receitas soma dos lançamentos de receita do mês.
 * @property despesas soma dos lançamentos de despesa (pagas e não pagas).
 * @property aPagar soma das despesas com `pago = false`.
 * @property restante `receitas − despesas` (pode ser negativo quando despesas superam receitas).
 */
data class Sumario(
	val receitas: Dinheiro,
	val despesas: Dinheiro,
	val aPagar: Dinheiro,
	val restante: Dinheiro,
) {
	companion object {
		/**
		 * Calcula o sumário a partir dos lançamentos do mês.
		 *
		 * O sumário é sempre derivado dos lançamentos fornecidos — não há
		 * estado, cache ou dependência externa. As alternâncias de
		 * investir/lazer não entram aqui (afetam só a distribuição das sobras).
		 */
		fun calcular(lancamentos: List<Lancamento>): Sumario {
			var receitas = Dinheiro.ZERO
			var despesas = Dinheiro.ZERO
			var aPagar = Dinheiro.ZERO
			for (lancamento in lancamentos) {
				when (lancamento) {
					is Lancamento.Receita -> receitas = receitas.sum(lancamento.valor)
					is Lancamento.Despesa -> {
						despesas = despesas.sum(lancamento.valor)
						if (!lancamento.pago) aPagar = aPagar.sum(lancamento.valor)
					}
				}
			}
			return Sumario(
				receitas = receitas,
				despesas = despesas,
				aPagar = aPagar,
				restante = receitas.subtractAllowingNegative(despesas),
			)
		}
	}
}
