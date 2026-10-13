package br.dev.brunorsch.ledger.server.domain

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DinheiroTest {

	// AC #1: normalização de escala e comparação por compareTo.
	@Test
	fun `string e BigDecimal com escalas diferentes tem compareTo zero`() {
		val deString = Dinheiro("1193.08")
		val deBigDecimal = Dinheiro(BigDecimal("1193.080"))

		assertThat(deString).isEqualByComparingTo(deBigDecimal)
	}

	// AC #2: mesma grandeza, escalas diferentes: compareTo == 0, equals == false.
	@Test
	fun `mesma grandeza com escalas diferentes tem compareTo zero mas equals false`() {
		val doisComUmaCasa = Dinheiro(BigDecimal("2.0"))
		val doisComDuasCasas = Dinheiro(BigDecimal("2.00"))

		assertThat(doisComUmaCasa).isEqualByComparingTo(doisComDuasCasas)
		assertFalse(doisComUmaCasa == doisComDuasCasas)
	}

	@Test
	fun `mesma grandeza com mesma escala tem equals true`() {
		assertEquals(Dinheiro(BigDecimal("2.00")), Dinheiro("2.00"))
	}

	// AC #3: HALF_UP no somar (e na normalização da criação).
	@Test
	fun `somar arredonda com HALF_UP`() {
		val resultado = Dinheiro("0.125").sum(Dinheiro("0"))

		assertThat(resultado).isEqualByComparingTo(Dinheiro("0.13"))
	}

	@Test
	fun `somar soma valores positivos`() {
		assertThat(Dinheiro("10.00").sum(Dinheiro("0.25"))).isEqualByComparingTo(Dinheiro("10.25"))
	}

	@Test
	fun `criacao arredonda escala maior que 2 com HALF_UP`() {
		assertThat(Dinheiro("0.124")).isEqualByComparingTo(Dinheiro("0.12"))
		assertThat(Dinheiro("0.125")).isEqualByComparingTo(Dinheiro("0.13"))
		assertThat(Dinheiro("0.135")).isEqualByComparingTo(Dinheiro("0.14"))
	}

	// AC #4: construtor padrão rejeita negativo com exceção explícita.
	@Test
	fun `construtor rejeita valor negativo com excecao explicita`() {
		val excecaoDeBigDecimal = assertFailsWith<IllegalArgumentException> { Dinheiro(BigDecimal("-0.01")) }
		val excecaoDeString = assertFailsWith<IllegalArgumentException> { Dinheiro("-1.00") }

		assertTrue(excecaoDeBigDecimal.message!!.contains("negativo"))
		assertTrue(excecaoDeString.message!!.contains("allowNegative"))
	}

	@Test
	fun `subtrair rejeita resultado negativo`() {
		assertThat(Dinheiro("10.00").subtract(Dinheiro("3.25"))).isEqualByComparingTo(Dinheiro("6.75"))
		assertFailsWith<IllegalArgumentException> { Dinheiro("1.00").subtract(Dinheiro("2.00")) }
	}

	// AC #5: factory explícita para sinal negativo (ex.: déficit do mês).
	@Test
	fun `allowNegative aceita valor negativo para deficit`() {
		val deficit = Dinheiro.allowNegative(BigDecimal("-5.00"))

		assertThat(deficit).isEqualByComparingTo(Dinheiro.allowNegative("-5.00"))
		assertTrue(deficit.compareTo(Dinheiro.ZERO) < 0)
	}

	@Test
	fun `allowNegative tambem normaliza a escala`() {
		assertThat(Dinheiro.allowNegative(BigDecimal("-1.234")))
			.isEqualByComparingTo(Dinheiro.allowNegative(BigDecimal("-1.23")))
	}

	// AC #6: ZERO é singleton igual a qualquer zero por compareTo.
	@Test
	fun `ZERO e singleton e igual a qualquer zero por compareTo`() {
		assertThat(Dinheiro.ZERO).isEqualByComparingTo(Dinheiro("0"))
		assertThat(Dinheiro.ZERO).isEqualByComparingTo(Dinheiro("0.0"))
		assertThat(Dinheiro.ZERO).isEqualByComparingTo(Dinheiro("0.00"))
	}

	// Operações restantes (plus/minus/times/divideBy das notas técnicas).
	@Test
	fun `multiplicarPor normaliza com HALF_UP`() {
		assertThat(Dinheiro("1.11").multiply(BigDecimal("2.5"))).isEqualByComparingTo(Dinheiro("2.78"))
	}

	@Test
	fun `divide usa a escala informada com HALF_UP e normaliza`() {
		assertThat(Dinheiro("10.00").divide(BigDecimal("3"), 2)).isEqualByComparingTo(Dinheiro("3.33"))
		assertThat(Dinheiro("1.00").divide(BigDecimal("8"), 2)).isEqualByComparingTo(Dinheiro("0.13"))
	}

	@Test
	fun `min retorna o menor entre dois Dinheiro`() {
		assertThat(Dinheiro("10.00").min(Dinheiro("20.00"))).isEqualByComparingTo(Dinheiro("10.00"))
		assertThat(Dinheiro("20.00").min(Dinheiro("10.00"))).isEqualByComparingTo(Dinheiro("10.00"))
		assertThat(Dinheiro("10.00").min(Dinheiro("10.00"))).isEqualByComparingTo(Dinheiro("10.00"))
	}

	@Test
	fun `abs devolve Dinheiro nao negativo igual ao valor absoluto`() {
		assertThat(Dinheiro("5.00").abs()).isEqualByComparingTo(Dinheiro("5.00"))
		assertThat(Dinheiro.ZERO.abs()).isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(Dinheiro.allowNegative("-5.00").abs()).isEqualByComparingTo(Dinheiro("5.00"))
	}

	@Test
	fun `subtractAllowingNegative devolve Dinheiro negativo quando o resultado e negativo`() {
		assertThat(Dinheiro("5.00").subtractAllowingNegative(Dinheiro("10.00")))
			.isEqualByComparingTo(Dinheiro.allowNegative("-5.00"))
		assertThat(Dinheiro("5.00").subtractAllowingNegative(Dinheiro("5.00")))
			.isEqualByComparingTo(Dinheiro.ZERO)
		assertThat(Dinheiro("10.00").subtractAllowingNegative(Dinheiro("3.00")))
			.isEqualByComparingTo(Dinheiro("7.00"))
	}
}
