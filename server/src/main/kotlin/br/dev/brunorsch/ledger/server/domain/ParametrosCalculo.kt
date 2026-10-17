package br.dev.brunorsch.ledger.server.domain

import java.math.BigDecimal

/**
 * Parâmetros de cálculo gravados em cada mês na criação.
 *
 * O passado não muda: alterar um parâmetro vale apenas para os meses criados
 * depois da alteração — cada mês usa a cópia gravada nele. O [versaoEsquema]
 * identifica o formato da estrutura e permite evoluí-la sem quebrar snapshots
 * antigas (que vivem para sempre enquanto o código muda).
 */
data class ParametrosCalculo(
	val versaoEsquema: Int,
	val percentuais: Percentuais,
	val tetos: Tetos,
) {
	data class Percentuais(
		val essenciais: BigDecimal,
		val lazer: BigDecimal,
	)

	data class Tetos(
		val essenciais: Dinheiro,
		val lazer: Dinheiro,
	)
}
