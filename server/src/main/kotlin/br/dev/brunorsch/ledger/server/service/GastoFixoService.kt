package br.dev.brunorsch.ledger.server.service

import br.dev.brunorsch.ledger.server.domain.GastoFixo
import br.dev.brunorsch.ledger.server.dto.AlterarGastoFixoRequest
import br.dev.brunorsch.ledger.server.dto.CriarGastoFixoRequest
import br.dev.brunorsch.ledger.server.exception.GastoFixoNaoEncontradoException
import br.dev.brunorsch.ledger.server.repository.GastoFixoRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GastoFixoService(private val gastoFixoRepository: GastoFixoRepository) {

	fun criar(usuarioId: Long, request: CriarGastoFixoRequest): GastoFixo {
		val gastoFixo = GastoFixo(
			descricao = request.descricao,
			valorPadrao = request.valorPadrao,
		)
		return gastoFixoRepository.criar(gastoFixo, usuarioId)
	}

	fun listar(usuarioId: Long): List<GastoFixo> = gastoFixoRepository.listarAtivos(usuarioId)

	fun alterar(idPublico: UUID, usuarioId: Long, request: AlterarGastoFixoRequest): GastoFixo {
		val atual = gastoFixoRepository.buscarPorIdPublicoEUsuario(idPublico, usuarioId)
			?: throw GastoFixoNaoEncontradoException()

		val atualizado = atual.copy(
			descricao = request.descricao ?: atual.descricao,
			valorPadrao = request.valorPadrao ?: atual.valorPadrao,
			ativo = request.ativo ?: atual.ativo,
		)
		gastoFixoRepository.atualizar(atualizado, usuarioId)
		return atualizado
	}

	fun remover(idPublico: UUID, usuarioId: Long) {
		if (!gastoFixoRepository.desativar(idPublico, usuarioId)) {
			throw GastoFixoNaoEncontradoException()
		}
	}
}
