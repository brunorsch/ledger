package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Mes

/** Alternâncias de investir e lazer do mês, como ficaram após o PATCH. */
data class AlternanciasResponse(
	val alternarInvestir: Boolean,
	val alternarLazer: Boolean,
) {
	companion object {
		fun fromDomain(mes: Mes): AlternanciasResponse = AlternanciasResponse(
			alternarInvestir = mes.alternarInvestir,
			alternarLazer = mes.alternarLazer,
		)
	}
}
