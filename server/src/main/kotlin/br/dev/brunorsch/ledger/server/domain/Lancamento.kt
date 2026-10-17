package br.dev.brunorsch.ledger.server.domain

import java.time.Instant
import java.util.UUID

/**
 * Lançamento de um mês: receita (entrada) ou despesa (saída) com valor monetário.
 *
 * Use [Lancamento.Receita] para entradas e [Lancamento.Despesa] para saídas.
 * O flag `pago` só faz sentido em [Lancamento.Despesa] — por isso a separação
 * em subtipos data class, em vez de um único tipo com campo nullable.
 *
 * O [id] interno nunca sai do backend; o [idPublico] é o identificador exposto
 * na API e é gerado pela aplicação na criação do lançamento.
 */
sealed class Lancamento {
	abstract val id: Long?
	abstract val idPublico: UUID
	abstract val valor: Dinheiro
	abstract val descricao: String

	/** Saída de dinheiro do mês. [pago] indica se a despesa já foi quitada. */
	data class Despesa(
		override val valor: Dinheiro,
		val pago: Boolean,
		override val descricao: String,
		val pagoEm: Instant? = null,
		override val id: Long? = null,
		override val idPublico: UUID = UUID.randomUUID(),
	) : Lancamento() {

		/**
		 * Aplica uma alteração de pagamento respeitando a regra do [pagoEm]:
		 * `pago=false` limpa o `pagoEm`; `pago=true` preenche com o informado,
		 * mantém o atual quando existir ou registra [agora] quando ausente.
		 */
		fun atualizarPago(novoPago: Boolean, novoPagoEm: Instant?, agora: Instant): Despesa =
			copy(
				pago = novoPago,
				pagoEm = when {
					!novoPago -> null
					novoPagoEm != null -> novoPagoEm
					pagoEm != null -> pagoEm
					else -> agora
				},
			)
	}

	/** Entrada de dinheiro no mês. */
	data class Receita(
		override val valor: Dinheiro,
		override val descricao: String,
		override val id: Long? = null,
		override val idPublico: UUID = UUID.randomUUID(),
	) : Lancamento()

	companion object {

		/** Valida que [valor] é maior que zero (regra de domínio dos lançamentos). */
		fun validarValorMaiorQueZero(valor: Dinheiro) {
			require(valor > Dinheiro.ZERO) {
				"valor deve ser maior que zero: ${valor.valor.toPlainString()}"
			}
		}
	}
}
