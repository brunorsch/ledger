package br.dev.brunorsch.ledger.server.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Mês do Ledger (agregado persistido em `meses`).
 *
 * Cada mês guarda seus próprios lançamentos concretos e os [ParametrosCalculo]
 * vigentes na criação, além das alternâncias de investir e lazer.
 * O [idPublico] é o identificador exposto na API; o [Id] interno nunca sai do backend.
 */
@Table("meses")
data class Mes(
	@Id val id: Long?,
	val idPublico: UUID,
	val usuarioId: Long,
	val mesReferencia: LocalDate,
	val parametrosCalculo: ParametrosCalculo,
	val alternarInvestir: Boolean,
	val alternarLazer: Boolean,
	val deficit: Dinheiro,
	val criadoEm: Instant,
) {
	companion object {

		/** Valida que [mesReferencia] é o primeiro dia do mês (regra de domínio). */
		fun validarMesReferenciaPrimeiroDia(mesReferencia: LocalDate) {
			require(mesReferencia.dayOfMonth == 1) {
				"mes_referencia deve ser o primeiro dia do mês: $mesReferencia"
			}
		}
	}
}
