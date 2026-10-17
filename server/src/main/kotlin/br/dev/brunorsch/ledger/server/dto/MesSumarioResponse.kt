package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.DistribuicaoSobras
import br.dev.brunorsch.ledger.server.domain.Sumario

/**
 * Sumário e distribuição das sobras de um mês expostos pela API.
 *
 * A distribuição é calculada com o snapshot de parâmetros e as alternâncias
 * gravados no mês; o [restante] pode ser negativo (déficit exposto em
 * [DistribuicaoSobras.deficit]).
 */
data class MesSumarioResponse(
	val receitas: Dinheiro,
	val despesas: Dinheiro,
	val aPagar: Dinheiro,
	val restante: Dinheiro,
	val distribuicao: DistribuicaoSobras,
) {
	companion object {
		fun fromDomain(sumario: Sumario, distribuicao: DistribuicaoSobras): MesSumarioResponse = MesSumarioResponse(
			receitas = sumario.receitas,
			despesas = sumario.despesas,
			aPagar = sumario.aPagar,
			restante = sumario.restante,
			distribuicao = distribuicao,
		)
	}
}
