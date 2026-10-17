package br.dev.brunorsch.ledger.server.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlin.test.Test
import java.time.Instant

class LancamentoTest {

	private val agora = Instant.parse("2026-10-16T12:00:00Z")

	@Test
	fun `valor maior que zero e valido`() {
		Lancamento.validarValorMaiorQueZero(Dinheiro("10.00"))
	}

	@Test
	fun `valor zero e invalido`() {
		assertThatThrownBy { Lancamento.validarValorMaiorQueZero(Dinheiro.ZERO) }
			.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessage("valor deve ser maior que zero: 0.00")
	}

	@Test
	fun `pago true sem pagoEm preenche com agora`() {
		val despesa = Lancamento.Despesa(Dinheiro("10.00"), pago = false, descricao = "mercado")

		val atualizada = despesa.atualizarPago(novoPago = true, novoPagoEm = null, agora = agora)

		assertThat(atualizada.pago).isTrue()
		assertThat(atualizada.pagoEm).isEqualTo(agora)
	}

	@Test
	fun `pago true com pagoEm informado usa o informado`() {
		val despesa = Lancamento.Despesa(Dinheiro("10.00"), pago = false, descricao = "mercado")
		val informado = Instant.parse("2026-10-10T10:00:00Z")

		val atualizada = despesa.atualizarPago(novoPago = true, novoPagoEm = informado, agora = agora)

		assertThat(atualizada.pagoEm).isEqualTo(informado)
	}

	@Test
	fun `pago true mantem pagoEm atual quando nenhum informado`() {
		val atual = Instant.parse("2026-10-10T10:00:00Z")
		val despesa = Lancamento.Despesa(Dinheiro("10.00"), pago = true, descricao = "mercado", pagoEm = atual)

		val atualizada = despesa.atualizarPago(novoPago = true, novoPagoEm = null, agora = agora)

		assertThat(atualizada.pagoEm).isEqualTo(atual)
	}

	@Test
	fun `pago false limpa pagoEm`() {
		val despesa = Lancamento.Despesa(Dinheiro("10.00"), pago = true, descricao = "mercado", pagoEm = agora)

		val atualizada = despesa.atualizarPago(novoPago = false, novoPagoEm = null, agora = agora)

		assertThat(atualizada.pago).isFalse()
		assertThat(atualizada.pagoEm).isNull()
	}

	@Test
	fun `pago false ignora pagoEm informado`() {
		val despesa = Lancamento.Despesa(Dinheiro("10.00"), pago = false, descricao = "mercado")

		val atualizada = despesa.atualizarPago(novoPago = false, novoPagoEm = agora, agora = agora)

		assertThat(atualizada.pago).isFalse()
		assertThat(atualizada.pagoEm).isNull()
	}
}
