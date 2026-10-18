package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.Mes
import br.dev.brunorsch.ledger.server.domain.ParametrosCalculo
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/** Mês exposto pela API: [id] é o id público (UUID), nunca o id interno. */
data class MesResponse(
	val id: UUID,
	val mesReferencia: LocalDate,
	val parametrosCalculo: ParametrosCalculo,
	val alternarInvestir: Boolean,
	val alternarLazer: Boolean,
	val deficit: Dinheiro,
	val criadoEm: Instant,
) {
	companion object {
		fun fromDomain(mes: Mes): MesResponse = MesResponse(
			id = mes.idPublico,
			mesReferencia = mes.mesReferencia,
			parametrosCalculo = mes.parametrosCalculo,
			alternarInvestir = mes.alternarInvestir,
			alternarLazer = mes.alternarLazer,
			deficit = mes.deficit,
			criadoEm = mes.criadoEm,
		)
	}
}
