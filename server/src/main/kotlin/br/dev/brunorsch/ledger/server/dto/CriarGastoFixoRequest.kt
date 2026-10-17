package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Dinheiro

/** Payload de criação de um gasto fixo. O gasto é criado ativo por padrão. */
data class CriarGastoFixoRequest(
	val descricao: String,
	val valorPadrao: Dinheiro,
)
