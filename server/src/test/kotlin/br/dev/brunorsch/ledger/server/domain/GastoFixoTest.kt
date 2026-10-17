package br.dev.brunorsch.ledger.server.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GastoFixoTest {

	@Test
	fun `paraLancamento gera despesa nao paga com descricao e valor do gasto fixo`() {
		val gastoFixo = GastoFixo(descricao = "aluguel", valorPadrao = Dinheiro("250.50"))

		val lancamento = gastoFixo.paraLancamento()

		assertThat(lancamento).isInstanceOf(Lancamento.Despesa::class.java)
		assertThat(lancamento.descricao).isEqualTo("aluguel")
		assertThat(lancamento.valor.compareTo(Dinheiro("250.50"))).isZero()
		assertThat(lancamento.pago).isFalse()
		assertThat(lancamento.origemGastoFixo).isTrue()
	}
}
