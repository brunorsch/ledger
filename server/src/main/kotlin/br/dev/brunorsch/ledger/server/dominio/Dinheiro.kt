package br.dev.brunorsch.ledger.server.dominio

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Valor monetário do Ledger.
 *
 * Regras do tipo:
 * - Sempre não negativo. O construtor padrão rejeita valores negativos com
 *   [IllegalArgumentException]. Para representar valores com sinal (ex.: déficit
 *   do mês), use [allowNegative].
 * - Sempre com no máximo 2 casas decimais. Valores recebidos com escala maior que 2
 *   são arredondados na criação com [RoundingMode.HALF_UP], o único modo de
 *   arredondamento do domínio. Ex.: `Dinheiro("0.125")` é `0.13`.
 * - Escalas menores que 2 são preservadas, então `equals`/`hashCode` seguem a
 *   semântica do [BigDecimal] subjacente, que considera a escala:
 *   `Dinheiro(BigDecimal("2.0")) == Dinheiro(BigDecimal("2.00"))` é `false`.
 *   Compare valores monetários sempre por [compareTo], nunca por `equals`.
 */
@JvmInline
value class Dinheiro private constructor(val valor: BigDecimal) : Comparable<Dinheiro> {

	override fun compareTo(outro: Dinheiro): Int = valor.compareTo(outro.valor)

	/** Soma e normaliza o resultado com HALF_UP. */
	fun somar(outro: Dinheiro): Dinheiro = Dinheiro.criar(valor.add(outro.valor))

	/**
	 * Subtrai e normaliza o resultado com HALF_UP. Lança [IllegalArgumentException]
	 * se o resultado for negativo; use [allowNegative] para representar déficit.
	 */
	fun subtrair(outro: Dinheiro): Dinheiro = Dinheiro.criar(valor.subtract(outro.valor))

	/** Multiplica por um fator e normaliza o resultado com HALF_UP. */
	fun multiplicarPor(fator: BigDecimal): Dinheiro = Dinheiro.criar(valor.multiply(fator))

	/** Divide por um divisor com a [escala] informada e HALF_UP, normalizando o resultado. */
	fun dividirPor(divisor: BigDecimal, escala: Int): Dinheiro =
		Dinheiro.criar(valor.divide(divisor, escala, RoundingMode.HALF_UP))

	companion object {

		/** Zero, singleton. */
		val ZERO: Dinheiro = Dinheiro(BigDecimal.ZERO.setScale(2))

		/** Cria a partir de uma string (ex.: `"1193.08"`), normalizando e validando. */
		operator fun invoke(valor: String): Dinheiro = criar(BigDecimal(valor))

		/** Cria a partir de um [BigDecimal], normalizando e validando. */
		operator fun invoke(valor: BigDecimal): Dinheiro = criar(valor)

		/** Cria a partir de uma string permitindo sinal negativo (ex.: déficit). */
		fun allowNegative(valor: String): Dinheiro = Dinheiro(normalizar(BigDecimal(valor)))

		/** Cria a partir de um [BigDecimal] permitindo sinal negativo (ex.: déficit). */
		fun allowNegative(valor: BigDecimal): Dinheiro = Dinheiro(normalizar(valor))

		private fun criar(valor: BigDecimal): Dinheiro {
			val normalizado = normalizar(valor)
			require(normalizado.signum() >= 0) {
				"Dinheiro não pode ser negativo: $normalizado. Use Dinheiro.allowNegative para valores negativos (ex.: déficit)."
			}
			return Dinheiro(normalizado)
		}

		private fun normalizar(valor: BigDecimal): BigDecimal =
			if (valor.scale() > 2) valor.setScale(2, RoundingMode.HALF_UP) else valor
	}
}
