package br.dev.brunorsch.ledger.server.dto

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.TipoLancamento
import java.time.Instant

/**
 * Payload de criação de um lançamento.
 *
 * `pago` e `pagoEm` só se aplicam a despesas: em receitas são ignorados.
 * Em despesas, `pago` é falso por padrão.
 */
data class CriarLancamentoRequest(
	val tipo: TipoLancamento,
	val descricao: String,
	val valor: Dinheiro,
	val pago: Boolean? = null,
	val pagoEm: Instant? = null,
)
