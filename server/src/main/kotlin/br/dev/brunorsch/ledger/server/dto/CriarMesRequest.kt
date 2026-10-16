package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.ParametrosCalculo
import java.time.LocalDate

/**
 * Payload de criação de um mês.
 *
 * As alternâncias de investir e lazer são opcionais e ligadas por padrão.
 */
data class CriarMesRequest(
	val mesReferencia: LocalDate,
	val parametrosCalculo: ParametrosCalculo,
	val alternarInvestir: Boolean = true,
	val alternarLazer: Boolean = true,
)
