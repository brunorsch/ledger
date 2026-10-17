package br.dev.brunorsch.ledger.server.repository

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.GastoFixo
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class GastoFixoRepository(private val jdbcTemplate: JdbcTemplate) {

	fun criar(gastoFixo: GastoFixo, usuarioId: Long): GastoFixo {
		val id = jdbcTemplate.queryForObject(
			"""
			INSERT INTO gastos_fixos (id_publico, usuario_id, descricao, valor_padrao, ativo)
			VALUES (?, ?, ?, ?, ?)
			RETURNING id
			""".trimIndent(),
			Long::class.java,
			gastoFixo.idPublico,
			usuarioId,
			gastoFixo.descricao,
			gastoFixo.valorPadrao.valor,
			gastoFixo.ativo,
		)!!
		return gastoFixo.copy(id = id)
	}

	fun listarAtivos(usuarioId: Long): List<GastoFixo> =
		jdbcTemplate.query(
			"""
			SELECT id, id_publico, descricao, valor_padrao, ativo
			FROM gastos_fixos
			WHERE usuario_id = ? AND ativo = true
			ORDER BY id
			""".trimIndent(),
			rowMapper,
			usuarioId,
		)

	fun buscarPorIdPublicoEUsuario(idPublico: UUID, usuarioId: Long): GastoFixo? =
		jdbcTemplate.query(
			"""
			SELECT id, id_publico, descricao, valor_padrao, ativo
			FROM gastos_fixos
			WHERE id_publico = ? AND usuario_id = ?
			""".trimIndent(),
			rowMapper,
			idPublico,
			usuarioId,
		).firstOrNull()

	fun atualizar(gastoFixo: GastoFixo, usuarioId: Long) {
		jdbcTemplate.update(
			"""
			UPDATE gastos_fixos
			SET descricao = ?, valor_padrao = ?, ativo = ?, atualizado_em = now()
			WHERE id = ? AND usuario_id = ?
			""".trimIndent(),
			gastoFixo.descricao,
			gastoFixo.valorPadrao.valor,
			gastoFixo.ativo,
			gastoFixo.id,
			usuarioId,
		)
	}

	fun desativar(idPublico: UUID, usuarioId: Long): Boolean =
		jdbcTemplate.update(
			"UPDATE gastos_fixos SET ativo = false, atualizado_em = now() WHERE id_publico = ? AND usuario_id = ?",
			idPublico,
			usuarioId,
		) == 1

	private val rowMapper = RowMapper { rs, _ ->
		GastoFixo(
			descricao = rs.getString("descricao"),
			valorPadrao = Dinheiro(rs.getBigDecimal("valor_padrao")),
			ativo = rs.getBoolean("ativo"),
			id = rs.getLong("id"),
			idPublico = rs.getObject("id_publico", UUID::class.java),
		)
	}
}
