package br.dev.brunorsch.ledger.server.domain

import java.util.UUID

/**
 * Modelo de gasto fixo do usuário: valor padrão replicado nos meses criados.
 *
 * O [id] interno nunca sai do backend; o [idPublico] é o identificador exposto
 * na API e é gerado pela aplicação na criação. Gastos fixos não são removidos,
 * apenas desativados ([ativo] = false), preservando o histórico.
 */
data class GastoFixo(
	val descricao: String,
	val valorPadrao: Dinheiro,
	val ativo: Boolean = true,
	val id: Long? = null,
	val idPublico: UUID = UUID.randomUUID(),
) {

	fun paraLancamento(): Lancamento.Despesa =
		Lancamento.Despesa(
			valor = valorPadrao,
			pago = false,
			descricao = descricao,
			origemGastoFixo = true,
		)
}
