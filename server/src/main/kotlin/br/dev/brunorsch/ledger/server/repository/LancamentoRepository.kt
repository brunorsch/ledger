package br.dev.brunorsch.ledger.server.repository

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.Lancamento
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LancamentoRepository(private val jdbcTemplate: JdbcTemplate) {

	fun criar(mesId: Long, usuarioId: Long, lancamento: Lancamento): Lancamento {
		val (tipo, pago, pagoEm) = colunasTipoPago(lancamento)
		val id = jdbcTemplate.queryForObject(
			"""
			INSERT INTO lancamentos (id_publico, usuario_id, mes_id, tipo, descricao, valor, pago, pago_em)
			VALUES (?, ?, ?, ?::tipo_lancamento, ?, ?, ?, ?)
			RETURNING id
			""".trimIndent(),
			Long::class.java,
			lancamento.idPublico,
			usuarioId,
			mesId,
			tipo,
			lancamento.descricao,
			lancamento.valor.valor,
			pago,
			pagoEm,
		)!!
		return lancamento.comId(id)
	}

	fun listarPorMes(mesId: Long, usuarioId: Long): List<Lancamento> =
		jdbcTemplate.query(
			"""
			SELECT id, id_publico, tipo, descricao, valor, pago, pago_em
			FROM lancamentos
			WHERE mes_id = ? AND usuario_id = ?
			ORDER BY id
			""".trimIndent(),
			rowMapper,
			mesId,
			usuarioId,
		)

	fun buscarPorIdPublicoEUsuario(idPublico: UUID, usuarioId: Long): Lancamento? =
		jdbcTemplate.query(
			"""
			SELECT id, id_publico, tipo, descricao, valor, pago, pago_em
			FROM lancamentos
			WHERE id_publico = ? AND usuario_id = ?
			""".trimIndent(),
			rowMapper,
			idPublico,
			usuarioId,
		).firstOrNull()

	fun atualizar(lancamento: Lancamento, usuarioId: Long) {
		val (_, pago, pagoEm) = colunasTipoPago(lancamento)
		jdbcTemplate.update(
			"""
			UPDATE lancamentos
			SET descricao = ?, valor = ?, pago = ?, pago_em = ?, atualizado_em = now()
			WHERE id = ? AND usuario_id = ?
			""".trimIndent(),
			lancamento.descricao,
			lancamento.valor.valor,
			pago,
			pagoEm,
			lancamento.id,
			usuarioId,
		)
	}

	fun removerPorIdPublicoEUsuario(idPublico: UUID, usuarioId: Long): Boolean =
		jdbcTemplate.update(
			"DELETE FROM lancamentos WHERE id_publico = ? AND usuario_id = ?",
			idPublico,
			usuarioId,
		) == 1

	private fun colunasTipoPago(lancamento: Lancamento): Triple<String, Boolean, java.sql.Timestamp?> =
		when (lancamento) {
			is Lancamento.Despesa -> Triple("DESPESA", lancamento.pago, lancamento.pagoEm?.let(java.sql.Timestamp::from))
			is Lancamento.Receita -> Triple("RECEITA", false, null)
		}

	private fun Lancamento.comId(id: Long): Lancamento =
		when (this) {
			is Lancamento.Despesa -> copy(id = id)
			is Lancamento.Receita -> copy(id = id)
		}

	private val rowMapper = RowMapper { rs, _ ->
		val valor = Dinheiro(rs.getBigDecimal("valor"))
		when (rs.getString("tipo")) {
			"RECEITA" -> Lancamento.Receita(
				valor = valor,
				descricao = rs.getString("descricao"),
				id = rs.getLong("id"),
				idPublico = rs.getObject("id_publico", UUID::class.java),
			)
			"DESPESA" -> Lancamento.Despesa(
				valor = valor,
				pago = rs.getBoolean("pago"),
				descricao = rs.getString("descricao"),
				pagoEm = rs.getTimestamp("pago_em")?.toInstant(),
				id = rs.getLong("id"),
				idPublico = rs.getObject("id_publico", UUID::class.java),
			)
			else -> throw IllegalStateException("tipo de lançamento desconhecido: ${rs.getString("tipo")}")
		}
	}
}
