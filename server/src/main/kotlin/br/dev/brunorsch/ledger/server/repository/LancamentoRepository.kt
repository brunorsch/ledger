package br.dev.brunorsch.ledger.server.repository

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.Lancamento
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class LancamentoRepository(private val jdbcTemplate: JdbcTemplate) {

	fun listarPorMes(mesId: Long, usuarioId: Long): List<Lancamento> =
		jdbcTemplate.query(
			"""
			SELECT tipo, valor, pago, descricao
			FROM lancamentos
			WHERE mes_id = ? AND usuario_id = ?
			ORDER BY id
			""".trimIndent(),
			rowMapper,
			mesId,
			usuarioId,
		)

	private val rowMapper = RowMapper { rs, _ ->
		val valor = Dinheiro(rs.getBigDecimal("valor"))
		when (rs.getString("tipo")) {
			"RECEITA" -> Lancamento.Receita(valor = valor, descricao = rs.getString("descricao"))
			"DESPESA" -> Lancamento.Despesa(
				valor = valor,
				pago = rs.getBoolean("pago"),
				descricao = rs.getString("descricao"),
			)
			else -> throw IllegalStateException("tipo de lançamento desconhecido: ${rs.getString("tipo")}")
		}
	}
}
