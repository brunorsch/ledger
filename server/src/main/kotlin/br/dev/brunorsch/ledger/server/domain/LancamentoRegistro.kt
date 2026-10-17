package br.dev.brunorsch.ledger.server.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Registro persistido de um lançamento (tabela `lancamentos`).
 *
 * A leitura do sumário usa apenas as colunas relevantes para o cálculo; o
 * CRUD de lançamentos entra em task dedicada e pode estender o mapeamento.
 */
@Table("lancamentos")
data class LancamentoRegistro(
	@Id val id: Long?,
	val usuarioId: Long,
	val mesId: Long,
	val tipo: TipoLancamento,
	val descricao: String,
	val valor: Dinheiro,
	val pago: Boolean,
) {
	/** Converte para o lançamento de domínio ([Lancamento.Receita] ou [Lancamento.Despesa]). */
	fun paraDominio(): Lancamento = when (tipo) {
		TipoLancamento.RECEITA -> Lancamento.Receita(valor = valor, descricao = descricao)
		TipoLancamento.DESPESA -> Lancamento.Despesa(valor = valor, pago = pago, descricao = descricao)
	}
}
