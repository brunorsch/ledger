package br.dev.brunorsch.ledger.server.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DistribuicaoSobrasTest {

	// AC #1: exemplos numéricos do projeto.
	@Test
	fun `restante 1193,08 com alternancias padrao`() {
		val d = DistribuicaoSobras.calcular(Dinheiro("1193.08"))
		assertEquals(0, d.essenciais.compareTo(Dinheiro("596.54")))
		assertEquals(0, d.lazer.compareTo(Dinheiro("357.92")))
		assertEquals(0, d.reservas.compareTo(Dinheiro("238.62")))
		assertEquals(0, d.deficit.compareTo(Dinheiro.ZERO))
	}

	@Test
	fun `restante 7872,34 com alternancias padrao`() {
		val d = DistribuicaoSobras.calcular(Dinheiro("7872.34"))
		assertEquals(0, d.essenciais.compareTo(Dinheiro("1300.00")))
		assertEquals(0, d.lazer.compareTo(Dinheiro("700.00")))
		assertEquals(0, d.reservas.compareTo(Dinheiro("5872.34")))
		assertEquals(0, d.deficit.compareTo(Dinheiro.ZERO))
	}

	// AC #2: soma sempre fecha no restante.
	@Test
	fun `soma essenciais mais lazer mais reservas fecha no restante em todos os cenarios`() {
		val restantes = listOf("0", "0.01", "100", "500", "1193.08", "1300", "5000", "7872.34", "10000")
		val combinacoes = listOf(
			true to true,
			false to true,
			true to false,
			false to false,
		)
		for (r in restantes) {
			for ((investir, lazer) in combinacoes) {
				val d = DistribuicaoSobras.calcular(Dinheiro(r), investir = investir, lazer = lazer)
				val soma = d.essenciais.sum(d.lazer).sum(d.reservas)
				assertEquals(
					0,
					soma.compareTo(Dinheiro(r)),
					"restante=$r investir=$investir lazer=$lazer: soma=$soma"
				)
			}
		}
	}

	// AC #3: restante == 0 → tudo zero, déficit zero.
	@Test
	fun `restante zero distribui zero em todas as categorias e deficit zero`() {
		val d = DistribuicaoSobras.calcular(Dinheiro.ZERO)
		assertEquals(0, d.essenciais.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.lazer.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.reservas.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.deficit.compareTo(Dinheiro.ZERO))
	}

	// AC #4: restante < 0 → tudo zero, déficit = |restante|.
	@Test
	fun `restante negativo expoe deficit igual ao valor absoluto e zera categorias`() {
		val d = DistribuicaoSobras.calcular(Dinheiro.allowNegative("-100.00"))
		assertEquals(0, d.essenciais.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.lazer.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.reservas.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.deficit.compareTo(Dinheiro("100.00")))
	}

	@Test
	fun `deficit usa o valor absoluto do restante negativo em varios cenarios`() {
		val cenarios = listOf("-1.00", "-50.00", "-100.50", "-1193.08", "-7872.34")
		for (negativo in cenarios) {
			val d = DistribuicaoSobras.calcular(Dinheiro.allowNegative(negativo))
			assertEquals(0, d.deficit.compareTo(Dinheiro(negativo.replace("-", ""))))
		}
	}

	// AC #5: investir=false, lazer=true → flex 70/30 (ess/laz), mantém limite de lazer.
	@Test
	fun `investir false e lazer true redistribui flex 70 30 entre essenciais e lazer`() {
		// restante 1193.08: flex = 596.54, 70% = 417.58 → ess; 30% = 178.96 → laz
		// ess = 596.54 + 417.58 = 1014.12; laz = 178.96; reservas = 0
		val d = DistribuicaoSobras.calcular(Dinheiro("1193.08"), investir = false, lazer = true)
		assertEquals(0, d.essenciais.compareTo(Dinheiro("1014.12")))
		assertEquals(0, d.lazer.compareTo(Dinheiro("178.96")))
		assertEquals(0, d.reservas.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.deficit.compareTo(Dinheiro.ZERO))
	}

	@Test
	fun `investir false e lazer true mantem limite de lazer 700 quando excede`() {
		// restante 7872.34: restanteAposEssenciais = 6572.34; 30% desse valor excede 700,
		// então lazer é capado em 700 e o excedente (5872.34) vai para essenciais.
		// ess = 1300 + 5872.34 = 7172.34; reservas = 0.
		val d = DistribuicaoSobras.calcular(Dinheiro("7872.34"), investir = false, lazer = true)
		assertEquals(0, d.essenciais.compareTo(Dinheiro("7172.34")))
		assertEquals(0, d.lazer.compareTo(Dinheiro("700.00")))
		assertEquals(0, d.reservas.compareTo(Dinheiro.ZERO))
	}

	// AC #6: investir=true, lazer=false → flex 50/50 (ess/res), respeita limite de gastos.
	@Test
	fun `investir true e lazer false redistribui flex 50 50 entre essenciais e reservas`() {
		// restante 1193.08: flex = 596.54, 50% = 298.27 → ess
		// ess = min(596.54 + 298.27, 1300) = 894.81; reservas = 298.27
		val d = DistribuicaoSobras.calcular(Dinheiro("1193.08"), investir = true, lazer = false)
		assertEquals(0, d.essenciais.compareTo(Dinheiro("894.81")))
		assertEquals(0, d.lazer.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.reservas.compareTo(Dinheiro("298.27")))
	}

	@Test
	fun `investir true e lazer false aplica cap de essenciais 1300`() {
		// restante 7872.34: flex = 6572.34, 50% = 3286.17 → ess capado em 1300
		// reservas = 7872.34 - 1300 = 6572.34
		val d = DistribuicaoSobras.calcular(Dinheiro("7872.34"), investir = true, lazer = false)
		assertEquals(0, d.essenciais.compareTo(Dinheiro("1300.00")))
		assertEquals(0, d.lazer.compareTo(Dinheiro.ZERO))
		assertEquals(0, d.reservas.compareTo(Dinheiro("6572.34")))
	}

	// AC #7: investir=false, lazer=false → tudo em essenciais, sem teto.
	@Test
	fun `investir false e lazer false aloca o restante todo em essenciais sem teto`() {
		val d1 = DistribuicaoSobras.calcular(Dinheiro("1193.08"), investir = false, lazer = false)
		assertEquals(0, d1.essenciais.compareTo(Dinheiro("1193.08")))
		assertEquals(0, d1.lazer.compareTo(Dinheiro.ZERO))
		assertEquals(0, d1.reservas.compareTo(Dinheiro.ZERO))

		// Garante que o teto de 1300 não é aplicado.
		val d2 = DistribuicaoSobras.calcular(Dinheiro("7872.34"), investir = false, lazer = false)
		assertEquals(0, d2.essenciais.compareTo(Dinheiro("7872.34")))
		assertEquals(0, d2.lazer.compareTo(Dinheiro.ZERO))
		assertEquals(0, d2.reservas.compareTo(Dinheiro.ZERO))
	}

	// AC #8: a fábrica não expõe parâmetro para essenciais — a invariante
	// "essenciais sempre ligado" é parte do algoritmo, então não há valor
	// inválido a rejeitar. Verificamos comportamentalmente: para qualquer
	// combinação de alternâncias e restante positivo, essenciais é positivo.
	@Test
	fun `essenciais sempre recebe valor positivo quando restante positivo`() {
		val restantes = listOf("0.01", "100.00", "1193.08", "7872.34")
		val combinacoes = listOf(
			true to true,
			false to true,
			true to false,
			false to false,
		)
		for (r in restantes) {
			for ((investir, lazer) in combinacoes) {
				val d = DistribuicaoSobras.calcular(Dinheiro(r), investir = investir, lazer = lazer)
				assertTrue(
					d.essenciais.compareTo(Dinheiro.ZERO) > 0,
					"restante=$r investir=$investir lazer=$lazer: essenciais=${d.essenciais}"
				)
			}
		}
	}

	// AC #9: determinístico.
	@Test
	fun `calcular e deterministico nas mesmas entradas`() {
		val r1 = DistribuicaoSobras.calcular(Dinheiro("1193.08"), investir = false, lazer = true)
		val r2 = DistribuicaoSobras.calcular(Dinheiro("1193.08"), investir = false, lazer = true)
		assertEquals(0, r1.essenciais.compareTo(r2.essenciais))
		assertEquals(0, r1.lazer.compareTo(r2.lazer))
		assertEquals(0, r1.reservas.compareTo(r2.reservas))
		assertEquals(0, r1.deficit.compareTo(r2.deficit))
	}

	// Garantia extra: o déficit é Dinheiro não negativo.
	@Test
	fun `deficit e Dinheiro nao negativo`() {
		val d = DistribuicaoSobras.calcular(Dinheiro.allowNegative("-50.00"))
		assertTrue(d.deficit.compareTo(Dinheiro.ZERO) > 0)
		assertEquals(0, d.deficit.compareTo(Dinheiro("50.00")))
	}

	@Test
	fun `categorias zeradas sao iguais a Dinheiro ZERO por compareTo`() {
		val d = DistribuicaoSobras.calcular(Dinheiro("1193.08"), investir = true, lazer = false)
		assertEquals(0, d.lazer.compareTo(Dinheiro.ZERO))
	}
}
