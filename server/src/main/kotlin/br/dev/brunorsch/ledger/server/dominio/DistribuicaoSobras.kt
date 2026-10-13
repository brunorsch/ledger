package br.dev.brunorsch.ledger.server.dominio

import java.math.BigDecimal

/**
 * Distribuição das sobras de um mês (receitas − despesas) entre as categorias
 * essenciais, lazer e reservas, com tratamento de déficit.
 *
 * Use [DistribuicaoSobras.calcular] para obter a distribuição a partir do
 * restante do mês e das alternâncias de investir e lazer.
 */
data class DistribuicaoSobras(
	val essenciais: Dinheiro,
	val lazer: Dinheiro,
	val reservas: Dinheiro,
	/** Exposto como `|restante|` quando `restante < 0`; `ZERO` caso contrário. */
	val deficit: Dinheiro,
) {
	companion object {
		private val LIMITE_ESSENCIAIS: Dinheiro = Dinheiro("1300.00")
		private val LIMITE_LAZER: Dinheiro = Dinheiro("700.00")
		private val PERCENTUAL_LAZER: BigDecimal = BigDecimal("0.30")
		private val DOIS: BigDecimal = BigDecimal(2)

		/**
		 * Calcula a distribuição das sobras do mês.
		 *
		 * Regra base (alternâncias padrão, ambas ligadas):
		 * - `essenciais = min(50% do restante, 1300)`
		 * - `lazer = min(30% do restante, 700)`
		 * - `reservas = restante - essenciais - lazer`
		 *
		 * Alternâncias:
		 * - `investir=false, lazer=true`: o restante após os essenciais base é
		 *   redistribuído — o lazer absorve sua fatia (limitada a 700) e o que
		 *   sobra vai para essenciais.
		 * - `investir=true, lazer=false`: o restante após os essenciais base é
		 *   dividido em duas metades — uma vai para essenciais (limitada a 1300)
		 *   e a outra para reservas. O excedente de essenciais permanece em
		 *   reservas.
		 * - `investir=false, lazer=false`: o restante vai inteiro para
		 *   essenciais, sem teto.
		 *
		 * Essenciais não tem alternância: é sempre tratado como ligado por
		 * construção do algoritmo. Por isso [calcular] só expõe `investir` e
		 * `lazer` — não há valor inválido que precise ser rejeitado.
		 *
		 * Quando `restante <= 0`, todas as categorias recebem zero e o déficit
		 * exposto é igual a `|restante|`. Modo de arredondamento: HALF_UP,
		 * consistente com [Dinheiro].
		 */
		fun calcular(
			restante: Dinheiro,
			investir: Boolean = true,
			lazer: Boolean = true,
		): DistribuicaoSobras {
			if (restante <= Dinheiro.ZERO) {
				return DistribuicaoSobras(
					essenciais = Dinheiro.ZERO,
					lazer = Dinheiro.ZERO,
					reservas = Dinheiro.ZERO,
					deficit = restante.abs(),
				)
			}

			val essenciaisBase = restante.divide(DOIS, 2).min(LIMITE_ESSENCIAIS)
			val restanteAposEssenciais = restante.subtract(essenciaisBase)
			val lazerBase = restante.multiply(PERCENTUAL_LAZER).min(LIMITE_LAZER)

			return when {
				investir && lazer -> {
					val reservas = restante.subtract(essenciaisBase).subtract(lazerBase)
					DistribuicaoSobras(
						essenciais = essenciaisBase,
						lazer = lazerBase,
						reservas = reservas,
						deficit = Dinheiro.ZERO,
					)
				}
				!investir && lazer -> {
					val adicionaisLazer = restanteAposEssenciais.multiply(PERCENTUAL_LAZER).min(LIMITE_LAZER)
					val adicionaisEssenciais = restanteAposEssenciais.subtract(adicionaisLazer)
					val essenciais = essenciaisBase.sum(adicionaisEssenciais)
					val reservas = restante.subtract(essenciais).subtract(adicionaisLazer)
					DistribuicaoSobras(
						essenciais = essenciais,
						lazer = adicionaisLazer,
						reservas = reservas,
						deficit = Dinheiro.ZERO,
					)
				}
				investir && !lazer -> {
					val adicionaisEssenciais = restanteAposEssenciais.divide(DOIS, 2)
					val essenciais = essenciaisBase.sum(adicionaisEssenciais).min(LIMITE_ESSENCIAIS)
					val reservas = restante.subtract(essenciais)
					DistribuicaoSobras(
						essenciais = essenciais,
						lazer = Dinheiro.ZERO,
						reservas = reservas,
						deficit = Dinheiro.ZERO,
					)
				}
				else -> DistribuicaoSobras(
					essenciais = restante,
					lazer = Dinheiro.ZERO,
					reservas = Dinheiro.ZERO,
					deficit = Dinheiro.ZERO,
				)
			}
		}
	}
}
