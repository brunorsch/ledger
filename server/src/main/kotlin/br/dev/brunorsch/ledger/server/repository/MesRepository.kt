package br.dev.brunorsch.ledger.server.repository

import br.dev.brunorsch.ledger.server.domain.Mes
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.util.UUID

interface MesRepository : CrudRepository<Mes, Long> {

	fun findByIdPublicoAndUsuarioId(idPublico: UUID, usuarioId: Long): Mes?

	fun findByMesReferenciaAndUsuarioId(mesReferencia: LocalDate, usuarioId: Long): Mes?
}
