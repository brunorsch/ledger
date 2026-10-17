package br.dev.brunorsch.ledger.server

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest
class MigracoesPostgresTest : SuportePostgres() {

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	@Test
	fun `as quatro tabelas existem`() {
		val tabelas = listOf("usuarios", "meses", "lancamentos", "gastos_fixos")

		val regclass = tabelas.map { nome ->
			jdbcTemplate.queryForObject("SELECT to_regclass('public.$nome')", String::class.java)
		}

		assertThat(regclass).doesNotContainNull()
	}

	@Test
	fun `tabela usuarios tem id bigint, sujeito_provedor varchar anulavel e criado_em timestamptz`() {
		val id = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'usuarios' AND column_name = 'id'",
			String::class.java,
		)
		val sujeitoProvedorTipo = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'usuarios' AND column_name = 'sujeito_provedor'",
			String::class.java,
		)
		val sujeitoProvedorAnulavel = jdbcTemplate.queryForObject(
			"SELECT is_nullable FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'usuarios' AND column_name = 'sujeito_provedor'",
			String::class.java,
		)
		val criadoEm = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'usuarios' AND column_name = 'criado_em'",
			String::class.java,
		)

		assertThat(id).isEqualTo("bigint")
		assertThat(sujeitoProvedorTipo).isEqualTo("character varying")
		assertThat(sujeitoProvedorAnulavel).isEqualTo("YES")
		assertThat(criadoEm).isEqualTo("timestamp with time zone")
	}

	@Test
	fun `tabela meses tem parametros_calculo jsonb obrigatorio e FK para usuarios`() {
		val tipoCaptura = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'meses' AND column_name = 'parametros_calculo'",
			String::class.java,
		)
		val capturaAnulavel = jdbcTemplate.queryForObject(
			"SELECT is_nullable FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'meses' AND column_name = 'parametros_calculo'",
			String::class.java,
		)
		val fkUsuarios = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM pg_constraint WHERE conrelid = 'meses'::regclass AND contype = 'f' AND confrelid = 'usuarios'::regclass",
			Long::class.java,
		)

		assertThat(tipoCaptura).isEqualTo("jsonb")
		assertThat(capturaAnulavel).isEqualTo("NO")
		assertThat(fkUsuarios).isEqualTo(1L)
	}

	@Test
	fun `tabela lancamentos tem enum tipo_lancamento, valor numeric obrigatorio, pago boolean e FKs`() {
		val valoresEnum = jdbcTemplate.queryForList(
			"SELECT unnest(enum_range(NULL::tipo_lancamento))",
			String::class.java,
		)
		val valorTipo = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'lancamentos' AND column_name = 'valor'",
			String::class.java,
		)
		val valorAnulavel = jdbcTemplate.queryForObject(
			"SELECT is_nullable FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'lancamentos' AND column_name = 'valor'",
			String::class.java,
		)
		val pagoTipo = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'lancamentos' AND column_name = 'pago'",
			String::class.java,
		)
		val origemGastoFixoTipo = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'lancamentos' AND column_name = 'origem_gasto_fixo'",
			String::class.java,
		)
		val fkUsuarios = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM pg_constraint WHERE conrelid = 'lancamentos'::regclass AND contype = 'f' AND confrelid = 'usuarios'::regclass",
			Long::class.java,
		)
		val fkMeses = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM pg_constraint WHERE conrelid = 'lancamentos'::regclass AND contype = 'f' AND confrelid = 'meses'::regclass",
			Long::class.java,
		)

		assertThat(valoresEnum).containsExactlyInAnyOrder("DESPESA", "RECEITA")
		assertThat(valorTipo).isEqualTo("numeric")
		assertThat(valorAnulavel).isEqualTo("NO")
		assertThat(pagoTipo).isEqualTo("boolean")
		assertThat(origemGastoFixoTipo).isEqualTo("boolean")
		assertThat(fkUsuarios).isEqualTo(1L)
		assertThat(fkMeses).isEqualTo(1L)
	}

	@Test
	fun `tabela gastos_fixos tem valor_padrao numeric obrigatorio, ativo boolean e FK para usuarios`() {
		val valorPadraoTipo = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'gastos_fixos' AND column_name = 'valor_padrao'",
			String::class.java,
		)
		val valorPadraoAnulavel = jdbcTemplate.queryForObject(
			"SELECT is_nullable FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'gastos_fixos' AND column_name = 'valor_padrao'",
			String::class.java,
		)
		val ativoTipo = jdbcTemplate.queryForObject(
			"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'gastos_fixos' AND column_name = 'ativo'",
			String::class.java,
		)
		val fkUsuarios = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM pg_constraint WHERE conrelid = 'gastos_fixos'::regclass AND contype = 'f' AND confrelid = 'usuarios'::regclass",
			Long::class.java,
		)

		assertThat(valorPadraoTipo).isEqualTo("numeric")
		assertThat(valorPadraoAnulavel).isEqualTo("NO")
		assertThat(ativoTipo).isEqualTo("boolean")
		assertThat(fkUsuarios).isEqualTo(1L)
	}

	@Test
	fun `seed insere um unico usuario com id 1`() {
		val linhas = jdbcTemplate.queryForList("SELECT id, sujeito_provedor FROM usuarios WHERE id = 1")

		assertThat(linhas).hasSize(1)
		assertThat(linhas[0]["sujeito_provedor"]).isNull()
	}

	@Test
	fun `existem indices de usuario_id nas tres tabelas e de mes_id em lancamentos`() {
		val meses = jdbcTemplate.queryForList("SELECT indexdef FROM pg_indexes WHERE schemaname = 'public' AND tablename = 'meses'", String::class.java)
		val lancamentos = jdbcTemplate.queryForList("SELECT indexdef FROM pg_indexes WHERE schemaname = 'public' AND tablename = 'lancamentos'", String::class.java)
		val gastosFixos = jdbcTemplate.queryForList("SELECT indexdef FROM pg_indexes WHERE schemaname = 'public' AND tablename = 'gastos_fixos'", String::class.java)

		assertThat(meses).anyMatch { it != null && it.contains("usuario_id") }
		assertThat(lancamentos).anyMatch { it != null && it.contains("usuario_id") }
		assertThat(lancamentos).anyMatch { it != null && it.contains("mes_id") }
		assertThat(gastosFixos).anyMatch { it != null && it.contains("usuario_id") }
	}

	@Test
	fun `colunas monetarias sao numeric 14 2 e nao ha double precision nem real`() {
		val colunasMonetarias = listOf(
			"lancamentos" to "valor",
			"gastos_fixos" to "valor_padrao",
			"meses" to "deficit",
		)

		for ((tabela, coluna) in colunasMonetarias) {
			val tipo = jdbcTemplate.queryForObject(
				"SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?",
				String::class.java,
				tabela,
				coluna,
			)
			val precisao = jdbcTemplate.queryForObject(
				"SELECT numeric_precision FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?",
				Int::class.java,
				tabela,
				coluna,
			)
			val escala = jdbcTemplate.queryForObject(
				"SELECT numeric_scale FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?",
				Int::class.java,
				tabela,
				coluna,
			)

			assertThat(tipo).isEqualTo("numeric")
			assertThat(precisao).isEqualTo(14)
			assertThat(escala).isEqualTo(2)
		}

		val tiposIncompativeis = jdbcTemplate.queryForList(
			"SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name IN ('usuarios', 'meses', 'lancamentos', 'gastos_fixos') AND data_type IN ('double precision', 'real')",
		)

		assertThat(tiposIncompativeis).isEmpty()
	}

	@Test
	fun `mes com parametros_calculo sem versao_esquema viola o check`() {
		assertThatThrownBy {
			jdbcTemplate.update(
				"INSERT INTO meses (id_publico, usuario_id, mes_referencia, parametros_calculo) VALUES (gen_random_uuid(), 1, '2026-10-01', '{}')",
			)
		}
			.isInstanceOf(DataIntegrityViolationException::class.java)
	}
}