package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.GastoFixo
import java.util.UUID

/**
 * Gasto fixo exposto pela API: [id] é o id público (UUID), nunca o id interno.
 */
data class GastoFixoResponse(
	val id: UUID,
	val descricao: String,
	val valorPadrao: Dinheiro,
	val ativo: Boolean,
) {

	companion object {

		fun fromDomain(gastoFixo: GastoFixo): GastoFixoResponse = GastoFixoResponse(
			id = gastoFixo.idPublico,
			descricao = gastoFixo.descricao,
			valorPadrao = gastoFixo.valorPadrao,
			ativo = gastoFixo.ativo,
		)
	}
}
