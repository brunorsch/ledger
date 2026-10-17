package br.dev.brunorsch.ledger.server.dto

/**
 * Payload do PATCH de alternâncias do mês.
 *
 * Os campos são opcionais: o que não for enviado mantém o valor atual do mês.
 * Não existe campo para essenciais — a alternância de gastos essenciais nunca
 * pode ser desligada, então a constraint é estrutural (TASK-4, AC #8).
 */
data class AlterarAlternanciasRequest(
	val alternarInvestir: Boolean? = null,
	val alternarLazer: Boolean? = null,
)
