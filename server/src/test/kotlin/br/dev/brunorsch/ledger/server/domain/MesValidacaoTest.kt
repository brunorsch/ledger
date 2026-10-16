package br.dev.brunorsch.ledger.server.domain

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MesValidacaoTest {

	@Test
	fun `primeiro dia do mes e valido`() {
		assertThatCode { Mes.validarMesReferenciaPrimeiroDia(LocalDate.of(2026, 10, 1)) }
			.doesNotThrowAnyException()
	}

	@Test
	fun `dia diferente do primeiro e invalido com mensagem clara`() {
		assertThatThrownBy { Mes.validarMesReferenciaPrimeiroDia(LocalDate.of(2026, 10, 15)) }
			.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessageContaining("primeiro dia do mês")
			.hasMessageContaining("2026-10-15")
	}
}
