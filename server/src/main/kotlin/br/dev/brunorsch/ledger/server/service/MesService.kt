package br.dev.brunorsch.ledger.server.service

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.Mes
import br.dev.brunorsch.ledger.server.dto.CriarMesRequest
import br.dev.brunorsch.ledger.server.exception.MesNaoEncontradoException
import br.dev.brunorsch.ledger.server.exception.ValidacaoException
import br.dev.brunorsch.ledger.server.repository.MesRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.util.UUID

@Service
class MesService(private val mesRepository: MesRepository) {

	fun criar(request: CriarMesRequest, usuarioId: Long): Mes {
		try {
			Mes.validarMesReferenciaPrimeiroDia(request.mesReferencia)
		} catch (ex: IllegalArgumentException) {
			throw ValidacaoException(ex.message!!)
		}

		val mes = Mes(
			id = null,
			idPublico = UUID.randomUUID(),
			usuarioId = usuarioId,
			mesReferencia = request.mesReferencia,
			parametrosCalculo = request.parametrosCalculo,
			alternarInvestir = request.alternarInvestir,
			alternarLazer = request.alternarLazer,
			deficit = Dinheiro.ZERO,
			criadoEm = Instant.now(),
		)
		return mesRepository.save(mes)
	}

	fun obterPorIdPublico(idPublico: UUID, usuarioId: Long): Mes =
		mesRepository.findByIdPublicoAndUsuarioId(idPublico, usuarioId)
			?: throw MesNaoEncontradoException()

	fun obterPorReferencia(referencia: String, usuarioId: Long): Mes {
		val primeiroDia = try {
			YearMonth.parse(referencia).atDay(1)
		} catch (ex: DateTimeParseException) {
			throw ValidacaoException("referencia deve estar no formato YYYY-MM: $referencia")
		}
		return mesRepository.findByMesReferenciaAndUsuarioId(primeiroDia, usuarioId)
			?: throw MesNaoEncontradoException()
	}
}
