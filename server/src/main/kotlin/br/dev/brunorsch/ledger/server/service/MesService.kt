package br.dev.brunorsch.ledger.server.service

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.DistribuicaoSobras
import br.dev.brunorsch.ledger.server.domain.Mes
import br.dev.brunorsch.ledger.server.domain.Sumario
import br.dev.brunorsch.ledger.server.dto.AlterarAlternanciasRequest
import br.dev.brunorsch.ledger.server.dto.AlternanciasResponse
import br.dev.brunorsch.ledger.server.dto.CriarMesRequest
import br.dev.brunorsch.ledger.server.dto.MesSumarioResponse
import br.dev.brunorsch.ledger.server.exception.MesJaExisteException
import br.dev.brunorsch.ledger.server.exception.MesNaoEncontradoException
import br.dev.brunorsch.ledger.server.exception.ValidacaoException
import br.dev.brunorsch.ledger.server.repository.GastoFixoRepository
import br.dev.brunorsch.ledger.server.repository.LancamentoRepository
import br.dev.brunorsch.ledger.server.repository.MesRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.util.UUID

@Service
class MesService(
	private val mesRepository: MesRepository,
	private val lancamentoRepository: LancamentoRepository,
	private val gastoFixoRepository: GastoFixoRepository,
) {

	fun criar(request: CriarMesRequest, usuarioId: Long): Mes {
		try {
			Mes.validarMesReferenciaPrimeiroDia(request.mesReferencia)
		} catch (ex: IllegalArgumentException) {
			throw ValidacaoException(ex.message!!)
		}

		if (mesRepository.findByMesReferenciaAndUsuarioId(request.mesReferencia, usuarioId) != null) {
			throw MesJaExisteException(request.mesReferencia.toString())
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
		val salvo = mesRepository.save(mes)
		gerarLancamentosDeGastosFixos(salvo.id!!, usuarioId)
		return salvo
	}

	/**
	 * Copia os gastos fixos ativos do usuário para o mês como despesas.
	 *
	 * A guarda evita duplicar os lançamentos quando o método roda mais de uma
	 * vez para o mesmo mês: só gera para meses que ainda não têm lançamentos
	 * originados de gastos fixos, mesmo que já existam lançamentos manuais.
	 */
	fun gerarLancamentosDeGastosFixos(mesId: Long, usuarioId: Long) {
		if (lancamentoRepository.existeOrigemGastoFixo(mesId, usuarioId)) {
			return
		}
		gastoFixoRepository.listarAtivos(usuarioId).forEach { gastoFixo ->
			lancamentoRepository.criar(mesId, usuarioId, gastoFixo.paraLancamento())
		}
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

	/**
	 * Sumário e distribuição das sobras de um mês, derivados dos lançamentos do
	 * próprio mês e do snapshot de parâmetros e alternâncias gravados nele.
	 */
	fun obterSumario(idPublico: UUID, usuarioId: Long): MesSumarioResponse {
		val mes = mesRepository.findByIdPublicoAndUsuarioId(idPublico, usuarioId)
			?: throw MesNaoEncontradoException()

		val sumario = Sumario.calcular(lancamentoRepository.listarPorMes(mes.id!!, usuarioId))
		val distribuicao = DistribuicaoSobras.calcular(
			restante = sumario.restante,
			parametros = mes.parametrosCalculo,
			investir = mes.alternarInvestir,
			lazer = mes.alternarLazer,
		)
		return MesSumarioResponse.fromDomain(sumario, distribuicao)
	}

	/**
	 * Altera as alternâncias de investir e lazer gravadas no mês.
	 *
	 * Campos não enviados no request mantêm o valor atual (atualização
	 * parcial); o snapshot de parâmetros e os demais campos ficam intactos.
	 */
	fun alterarAlternancias(
		idPublico: UUID,
		usuarioId: Long,
		request: AlterarAlternanciasRequest,
	): AlternanciasResponse {
		val mes = mesRepository.findByIdPublicoAndUsuarioId(idPublico, usuarioId)
			?: throw MesNaoEncontradoException()

		val atualizado = mes.copy(
			alternarInvestir = request.alternarInvestir ?: mes.alternarInvestir,
			alternarLazer = request.alternarLazer ?: mes.alternarLazer,
		)
		return AlternanciasResponse.fromDomain(mesRepository.save(atualizado))
	}
}
