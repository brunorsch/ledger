package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import java.time.Instant

/**
 * Payload do PATCH de um lançamento.
 *
 * Os campos são opcionais: o que não for enviado mantém o valor atual.
 * Em receitas, `pago` e `pagoEm` são ignorados.
 */
data class AlterarLancamentoRequest(
	val descricao: String? = null,
	val valor: Dinheiro? = null,
	val pago: Boolean? = null,
	val pagoEm: Instant? = null,
)
