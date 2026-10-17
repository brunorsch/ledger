package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Dinheiro

/** Payload do PATCH de um gasto fixo: campos não enviados mantêm o valor atual. */
data class AlterarGastoFixoRequest(
	val descricao: String? = null,
	val valorPadrao: Dinheiro? = null,
	val ativo: Boolean? = null,
)
