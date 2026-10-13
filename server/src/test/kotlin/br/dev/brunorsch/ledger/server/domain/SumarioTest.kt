package br.dev.brunorsch.ledger.server.domain

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SumarioTest {

	// AC #1: mês sem lançamentos → todos os campos zero.
	@Test
	fun `mes sem lancamentos retorna todos os campos zero`() {
		val resultado = Sumario.calcular(emptyList())

		assertThat(resultado.receitas).isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(resultado.despesas).isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(resultado.aPagar).isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(resultado.restante).isEqualByComparingTo(Dinheiro.ZERO)
	}

	// AC #2: Receitas soma todos os lançamentos de receita.
	@Test
	fun `receitas soma todos os lancamentos de receita do mes`() {
		val lancamentos = listOf(
			Lancamento.Receita(Dinheiro("1000.00"), descricao = "salário"),
			Lancamento.Receita(Dinheiro("200.50"), descricao = "freelance"),
		)

		val resultado = Sumario.calcular(lancamentos)

		assertThat(resultado.receitas).isEqualByComparingTo(Dinheiro("1200.50"))
		assertThat(resultado.despesas).isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(resultado.aPagar).isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(resultado.restante).isEqualByComparingTo(Dinheiro("1200.50"))
	}

	// AC #3: Despesas soma todos os lançamentos de despesa (pagas + não pagas).
	@Test
	fun `despesas soma todos os lancamentos de despesa do mes`() {
		val lancamentos = listOf(
			Lancamento.Despesa(Dinheiro("300.00"), pago = true, descricao = "aluguel"),
			Lancamento.Despesa(Dinheiro("50.00"), pago = false, descricao = "luz"),
		)

		val resultado = Sumario.calcular(lancamentos)

		assertThat(resultado.receitas).isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(resultado.despesas).isEqualByComparingTo(Dinheiro("350.00"))
	}

	// AC #4: A pagar soma apenas despesas com pago = false.
	@Test
	fun `a pagar soma apenas despesas nao pagas`() {
		val lancamentos = listOf(
			Lancamento.Despesa(Dinheiro("300.00"), pago = true, descricao = "aluguel"),
			Lancamento.Despesa(Dinheiro("50.00"), pago = false, descricao = "luz"),
			Lancamento.Despesa(Dinheiro("25.00"), pago = false, descricao = "água"),
		)

		val resultado = Sumario.calcular(lancamentos)

		assertThat(resultado.aPagar).isEqualByComparingTo(Dinheiro("75.00"))
		assertThat(resultado.despesas).isEqualByComparingTo(Dinheiro("375.00"))
	}

	// AC #5: Restante = Receitas - Despesas, pode ser negativo.
	@Test
	fun `restante positivo quando receitas superam despesas`() {
		val lancamentos = listOf(
			Lancamento.Receita(Dinheiro("1000.00"), descricao = "salário"),
			Lancamento.Despesa(Dinheiro("300.00"), pago = true, descricao = "aluguel"),
		)

		val resultado = Sumario.calcular(lancamentos)

		assertThat(resultado.restante).isEqualByComparingTo(Dinheiro("700.00"))
	}

	@Test
	fun `restante negativo quando despesas superam receitas`() {
		val lancamentos = listOf(
			Lancamento.Receita(Dinheiro("500.00"), descricao = "salário"),
			Lancamento.Despesa(Dinheiro("800.00"), pago = false, descricao = "cartão"),
		)

		val resultado = Sumario.calcular(lancamentos)

		assertThat(resultado.restante).isEqualByComparingTo(Dinheiro.allowNegative("-300.00"))
	}

	// AC #6: Alterar pago de uma despesa recalcula A pagar.
	@Test
	fun `alterar pago recalcula a pagar`() {
		val pago = listOf(
			Lancamento.Despesa(Dinheiro("100.00"), pago = true, descricao = "x"),
		)
		val naoPago = listOf(
			Lancamento.Despesa(Dinheiro("100.00"), pago = false, descricao = "x"),
		)

		val resultadoPago = Sumario.calcular(pago)
		val resultadoNaoPago = Sumario.calcular(naoPago)

		assertThat(resultadoPago.aPagar).isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(resultadoNaoPago.aPagar).isEqualByComparingTo(Dinheiro("100.00"))
		assertThat(resultadoPago.despesas).isEqualByComparingTo(resultadoNaoPago.despesas)
		assertThat(resultadoPago.restante).isEqualByComparingTo(resultadoNaoPago.restante)
	}

	// AC #7: Adicionar ou remover lançamento recalcula o sumário.
	@Test
	fun `adicionar lancamento recalcula o sumario`() {
		val antes = Sumario.calcular(listOf(
			Lancamento.Receita(Dinheiro("100.00"), descricao = "x"),
		))
		val depois = Sumario.calcular(listOf(
			Lancamento.Receita(Dinheiro("100.00"), descricao = "x"),
			Lancamento.Receita(Dinheiro("50.00"), descricao = "y"),
		))

		assertThat(antes.receitas).isEqualByComparingTo(Dinheiro("100.00"))
		assertThat(depois.receitas).isEqualByComparingTo(Dinheiro("150.00"))
		assertThat(antes.restante).isEqualByComparingTo(Dinheiro("100.00"))
		assertThat(depois.restante).isEqualByComparingTo(Dinheiro("150.00"))
	}

	@Test
	fun `remover lancamento recalcula o sumario`() {
		val antes = Sumario.calcular(listOf(
			Lancamento.Receita(Dinheiro("100.00"), descricao = "x"),
			Lancamento.Receita(Dinheiro("50.00"), descricao = "y"),
		))
		val depois = Sumario.calcular(listOf(
			Lancamento.Receita(Dinheiro("100.00"), descricao = "x"),
		))

		assertThat(antes.receitas).isEqualByComparingTo(Dinheiro("150.00"))
		assertThat(depois.receitas).isEqualByComparingTo(Dinheiro("100.00"))
	}

	// AC #8: Determinístico.
	@Test
	fun `calcular e deterministico para os mesmos lancamentos`() {
		val lancamentos = listOf(
			Lancamento.Receita(Dinheiro("5000.00"), descricao = "salário"),
			Lancamento.Despesa(Dinheiro("1500.00"), pago = true, descricao = "aluguel"),
			Lancamento.Despesa(Dinheiro("100.00"), pago = false, descricao = "luz"),
		)

		val resultado1 = Sumario.calcular(lancamentos)
		val resultado2 = Sumario.calcular(lancamentos)

		assertThat(resultado1.receitas).isEqualByComparingTo(resultado2.receitas)
		assertThat(resultado1.despesas).isEqualByComparingTo(resultado2.despesas)
		assertThat(resultado1.aPagar).isEqualByComparingTo(resultado2.aPagar)
		assertThat(resultado1.restante).isEqualByComparingTo(resultado2.restante)
	}

	// Cobertura adicional: cenário misto verifica a interação entre os campos.
	@Test
	fun `cenario misto calcula todos os campos corretamente`() {
		val lancamentos = listOf(
			Lancamento.Receita(Dinheiro("5000.00"), descricao = "salário"),
			Lancamento.Receita(Dinheiro("200.00"), descricao = "freelance"),
			Lancamento.Despesa(Dinheiro("1500.00"), pago = true, descricao = "aluguel"),
			Lancamento.Despesa(Dinheiro("300.00"), pago = true, descricao = "mercado"),
			Lancamento.Despesa(Dinheiro("100.00"), pago = false, descricao = "luz"),
			Lancamento.Despesa(Dinheiro("50.00"), pago = false, descricao = "água"),
		)

		val resultado = Sumario.calcular(lancamentos)

		assertThat(resultado.receitas).isEqualByComparingTo(Dinheiro("5200.00"))
		assertThat(resultado.despesas).isEqualByComparingTo(Dinheiro("1950.00"))
		assertThat(resultado.aPagar).isEqualByComparingTo(Dinheiro("150.00"))
		assertThat(resultado.restante).isEqualByComparingTo(Dinheiro("3250.00"))
	}

	// Cobre os métodos auto-gerados dos data classes Lancamento.Despesa/Receita (toString/copy/equals/hashCode)
	// e a hierarquia sealed (smart cast em when).
	@Test
	fun `Lancamento expõe os campos via toString copy e equality de data class`() {
		val despesa = Lancamento.Despesa(Dinheiro("100.00"), pago = true, descricao = "x")
		val despesaCopia = despesa.copy(pago = false)

		assertThat(despesaCopia.valor).isEqualByComparingTo(despesa.valor)
		assertFalse(despesaCopia.pago)
		assertEquals("x", despesaCopia.descricao)
		assertTrue(despesaCopia is Lancamento.Despesa)

		val receita = Lancamento.Receita(Dinheiro("200.00"), descricao = "y")

		assertThat(receita.valor).isEqualByComparingTo(Dinheiro("200.00"))
		assertEquals("y", receita.descricao)
		assertTrue(receita is Lancamento.Receita)
		assertFalse(despesa == Lancamento.Receita(Dinheiro("100.00"), descricao = "x"))
	}

	@Test
	fun `Lancamento sealed permite when exaustivo sobre os subtipos`() {
		val lancamentos: List<Lancamento> = listOf(
			Lancamento.Receita(Dinheiro("100.00"), descricao = "r"),
			Lancamento.Despesa(Dinheiro("50.00"), pago = true, descricao = "d"),
		)

		val tipos = lancamentos.map {
			when (it) {
				is Lancamento.Receita -> "receita"
				is Lancamento.Despesa -> "despesa"
			}
		}

		assertEquals(listOf("receita", "despesa"), tipos)
	}
}
