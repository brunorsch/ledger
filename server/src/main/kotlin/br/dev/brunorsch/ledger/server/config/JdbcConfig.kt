package br.dev.brunorsch.ledger.server.config

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import br.dev.brunorsch.ledger.server.domain.ParametrosCalculo
import br.dev.brunorsch.ledger.server.domain.TipoLancamento
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal

/**
 * Conversões extras do Spring Data JDBC:
 * - `ParametrosCalculo` ↔ `PGobject` (`jsonb`), serializando o JSON com o
 *   mesmo [JsonMapper] da aplicação (Dinheiro como string).
 * - `Dinheiro` ↔ `BigDecimal` para colunas `NUMERIC(14,2)` (ex.: deficit).
 * - `TipoLancamento` ← `PGobject` para a coluna enum `lancamentos.tipo`
 *   (a escrita entra com o CRUD de lançamentos).
 */
@Configuration
class JdbcConfig(objectMapper: JsonMapper) : AbstractJdbcConfiguration() {

	private val parametrosCalculoWritingConverter = ParametrosCalculoWritingConverter(objectMapper)
	private val parametrosCalculoReadingConverter = ParametrosCalculoReadingConverter(objectMapper)
	private val dinheiroWritingConverter = DinheiroWritingConverter()
	private val dinheiroReadingConverter = DinheiroReadingConverter()
	private val tipoLancamentoReadingConverter = TipoLancamentoReadingConverter()

	override fun userConverters(): List<*> = listOf(
		parametrosCalculoWritingConverter,
		parametrosCalculoReadingConverter,
		dinheiroWritingConverter,
		dinheiroReadingConverter,
		tipoLancamentoReadingConverter,
	)

	@WritingConverter
	class ParametrosCalculoWritingConverter(
		private val objectMapper: JsonMapper,
	) : Converter<ParametrosCalculo, PGobject> {

		override fun convert(source: ParametrosCalculo): PGobject = PGobject().apply {
			type = "jsonb"
			value = objectMapper.writeValueAsString(source)
		}
	}

	@ReadingConverter
	class ParametrosCalculoReadingConverter(
		private val objectMapper: JsonMapper,
	) : Converter<PGobject, ParametrosCalculo> {

		override fun convert(source: PGobject): ParametrosCalculo =
			objectMapper.readValue(source.value, ParametrosCalculo::class.java)
	}

	@WritingConverter
	class DinheiroWritingConverter : Converter<Dinheiro, BigDecimal> {

		override fun convert(source: Dinheiro): BigDecimal = source.valor
	}

	@ReadingConverter
	class DinheiroReadingConverter : Converter<BigDecimal, Dinheiro> {

		override fun convert(source: BigDecimal): Dinheiro = Dinheiro(source)
	}

	@ReadingConverter
	class TipoLancamentoReadingConverter : Converter<PGobject, TipoLancamento> {

		override fun convert(source: PGobject): TipoLancamento = TipoLancamento.valueOf(source.value!!)
	}
}
