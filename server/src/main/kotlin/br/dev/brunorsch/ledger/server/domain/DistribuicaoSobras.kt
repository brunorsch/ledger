package br.dev.brunorsch.ledger.server.domain

import java.math.BigDecimal

/**
 * Distribuição das sobras de um mês (receitas − despesas) entre as categorias
 * essenciais, lazer e reservas, com tratamento de déficit.
 *
 * Use [DistribuicaoSobras.calcular] para obter a distribuição a partir do
 * restante do mês, do snapshot de parâmetros gravado no mês e das alternâncias
 * de investir e lazer.
 */
data class DistribuicaoSobras(
	val essenciais: Dinheiro,
	val lazer: Dinheiro,
	val reservas: Dinheiro,
	/** Exposto como `|restante|` quando `restante < 0`; `ZERO` caso contrário. */
	val deficit: Dinheiro,
) {
	companion object {
		private val DOIS: BigDecimal = BigDecimal(2)

		/**
		 * Calcula a distribuição das sobras do mês.
		 *
		 * Os percentuais e tetos vêm do [parametros], o snapshot gravado no
		 * mês na criação — nunca parâmetros globais atuais. Regra base
		 * (alternâncias padrão, ambas ligadas):
		 * - `essenciais = min(percentual de essenciais × restante, teto de essenciais)`
		 * - `lazer = min(percentual de lazer × restante, teto de lazer)`
		 * - `reservas = restante - essenciais - lazer`
		 *
		 * Alternâncias:
		 * - `investir=false, lazer=true`: o restante após os essenciais base é
		 *   redistribuído — o lazer absorve sua fatia (percentual de lazer,
		 *   limitada ao teto de lazer) e o que sobra vai para essenciais.
		 * - `investir=true, lazer=false`: o restante após os essenciais base é
		 *   dividido em duas metades — uma vai para essenciais (limitada ao
		 *   teto de essenciais) e a outra para reservas. O excedente de
		 *   essenciais permanece em reservas.
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
			parametros: ParametrosCalculo,
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

			val percentualEssenciais = parametros.percentuais.essenciais
			val percentualLazer = parametros.percentuais.lazer
			val tetoEssenciais = parametros.tetos.essenciais
			val tetoLazer = parametros.tetos.lazer

			val essenciaisBase = restante.multiply(percentualEssenciais).min(tetoEssenciais)
			val restanteAposEssenciais = restante.subtract(essenciaisBase)
			val lazerBase = restante.multiply(percentualLazer).min(tetoLazer)

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
					val adicionaisLazer = restanteAposEssenciais.multiply(percentualLazer).min(tetoLazer)
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
					val essenciais = essenciaisBase.sum(adicionaisEssenciais).min(tetoEssenciais)
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
