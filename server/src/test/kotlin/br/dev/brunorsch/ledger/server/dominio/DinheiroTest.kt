package br.dev.brunorsch.ledger.server.dominio

import java.math.BigDecimal
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
		assertEquals(0, deString.compareTo(deBigDecimal))
	}

	// AC #2: mesma grandeza, escalas diferentes: compareTo == 0, equals == false.
	@Test
	fun `mesma grandeza com escalas diferentes tem compareTo zero mas equals false`() {
		val doisComUmaCasa = Dinheiro(BigDecimal("2.0"))
		val doisComDuasCasas = Dinheiro(BigDecimal("2.00"))
		assertEquals(0, doisComUmaCasa.compareTo(doisComDuasCasas))
		assertFalse(doisComUmaCasa == doisComDuasCasas)
	}

	@Test
	fun `mesma grandeza com mesma escala tem equals true`() {
		assertEquals(Dinheiro(BigDecimal("2.00")), Dinheiro("2.00"))
	}

	// AC #3: HALF_UP no somar (e na normalização da criação).
	@Test
	fun `somar arredonda com HALF_UP`() {
		val resultado = Dinheiro("0.125").somar(Dinheiro("0"))
		assertEquals(0, resultado.compareTo(Dinheiro("0.13")))
	}

	@Test
	fun `somar soma valores positivos`() {
		assertEquals(0, Dinheiro("10.00").somar(Dinheiro("0.25")).compareTo(Dinheiro("10.25")))
	}

	@Test
	fun `criacao arredonda escala maior que 2 com HALF_UP`() {
		assertEquals(0, Dinheiro("0.124").compareTo(Dinheiro("0.12")))
		assertEquals(0, Dinheiro("0.125").compareTo(Dinheiro("0.13")))
		assertEquals(0, Dinheiro("0.135").compareTo(Dinheiro("0.14")))
	}

	// AC #4: construtor padrão rejeita negativo com exceção explícita.
	@Test
	fun `construtor rejeita valor negativo com excecao explicita`() {
		val excecaoDeBigDecimal = assertFailsWith<IllegalArgumentException> { Dinheiro(BigDecimal("-0.01")) }
		assertTrue(excecaoDeBigDecimal.message!!.contains("negativo"))
		val excecaoDeString = assertFailsWith<IllegalArgumentException> { Dinheiro("-1.00") }
		assertTrue(excecaoDeString.message!!.contains("allowNegative"))
	}

	@Test
	fun `subtrair rejeita resultado negativo`() {
		assertEquals(0, Dinheiro("10.00").subtrair(Dinheiro("3.25")).compareTo(Dinheiro("6.75")))
		assertFailsWith<IllegalArgumentException> { Dinheiro("1.00").subtrair(Dinheiro("2.00")) }
	}

	// AC #5: factory explícita para sinal negativo (ex.: déficit do mês).
	@Test
	fun `allowNegative aceita valor negativo para deficit`() {
		val deficit = Dinheiro.allowNegative(BigDecimal("-5.00"))
		assertEquals(0, deficit.compareTo(Dinheiro.allowNegative("-5.00")))
		assertTrue(deficit.compareTo(Dinheiro.ZERO) < 0)
	}

	@Test
	fun `allowNegative tambem normaliza a escala`() {
		assertEquals(0, Dinheiro.allowNegative(BigDecimal("-1.234")).compareTo(Dinheiro.allowNegative(BigDecimal("-1.23"))))
	}

	// AC #6: ZERO é singleton igual a qualquer zero por compareTo.
	@Test
	fun `ZERO e singleton e igual a qualquer zero por compareTo`() {
		assertEquals(0, Dinheiro.ZERO.compareTo(Dinheiro("0")))
		assertEquals(0, Dinheiro.ZERO.compareTo(Dinheiro("0.0")))
		assertEquals(0, Dinheiro.ZERO.compareTo(Dinheiro("0.00")))
	}

	// Operações restantes (plus/minus/times/divideBy das notas técnicas).
	@Test
	fun `multiplicarPor normaliza com HALF_UP`() {
		assertEquals(0, Dinheiro("1.11").multiplicarPor(BigDecimal("2.5")).compareTo(Dinheiro("2.78")))
	}

	@Test
	fun `dividirPor usa a escala informada com HALF_UP e normaliza`() {
		assertEquals(0, Dinheiro("10.00").dividirPor(BigDecimal("3"), 2).compareTo(Dinheiro("3.33")))
		assertEquals(0, Dinheiro("1.00").dividirPor(BigDecimal("8"), 2).compareTo(Dinheiro("0.13")))
	}
}
