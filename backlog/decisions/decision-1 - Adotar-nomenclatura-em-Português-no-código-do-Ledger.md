---
id: decision-1
title: Adotar nomenclatura em Português no código do Ledger
date: '2026-10-11 17:42'
status: accepted
---
## Context

A planilha de referência do Ledger é inteiramente em Português, e o produto fala com um único usuário brasileiro que lê o código. Misturar identificadores em inglês com regras de negócio em Português produz "portunhol" no médio prazo: nomes como `Month`, `Entry`, `FixedExpense`, `paid`, `description`, `value`, `user_id`, `reference_month`, `parameters_snapshot`, `schema_version`, `provider_subject`, `default_value`, `active`, `created_at` e `updated_at` precisam ser traduzidos para o vocabulário do domínio (`Mês`, `Lançamento`, `GastoFixo`, `pago`, `descricao`, `valor`, `usuario_id`, `mes_referencia`, `captura_parametros`, `versao_esquema`, `sujeito_provedor`, `valor_padrao`, `ativo`, `criado_em`, `atualizado_em`). Os termos puramente técnicos (nomes de bibliotecas, tipos da JVM, anotações Spring/JPA, comandos de banco, provedores OAuth) permanecem em inglês para evitar ruído desnecessário.

## Decision

Adotar nomenclatura em Português para todo termo de domínio na codebase (backend Kotlin, frontend React, migrations Flyway, contratos JSON da API e rotas HTTP). Tradução canônica:

| Domínio (inglês) | Domínio (português) |
| --- | --- |
| `Money` (value class) | `Dinheiro` |
| `Month` (entidade e tabela) | `Mes` / `meses` |
| `Entry` (entidade e tabela) | `Lancamento` / `lancamentos` |
| `FixedExpense` | `GastoFixo` / `gastos_fixos` |
| `User` / `users` | `Usuario` / `usuarios` |
| `Summary` | `Sumario` |
| `Distribution` | `Distribuicao` |
| `Toggle` (UI verb) | `Alternancia` (substantivo) / `alternar` (verbo) |
| `Snapshot` | `captura` |
| `Provider` (OAuth) | `Provedor` |
| `Schema version` | `versao_esquema` |

Colunas e chaves JSON:

| Inglês | Português (snake_case) | Português (camelCase JSON) |
| --- | --- | --- |
| `id` | `id` | `id` |
| `user_id` | `usuario_id` | `usuarioId` |
| `month_id` | `mes_id` | `mesId` |
| `provider_subject` | `sujeito_provedor` | `sujeitoProvedor` |
| `reference_month` | `mes_referencia` | `mesReferencia` |
| `parameters_snapshot` | `captura_parametros` | `capturaParametros` |
| `schema_version` | `versao_esquema` | `versaoEsquema` |
| `toggles_investir` | `alternar_investir` | `alternarInvestir` |
| `toggles_lazer` | `alternar_lazer` | `alternarLazer` |
| `deficit` | `deficit` | `deficit` |
| `kind` | `tipo` | `tipo` |
| `description` | `descricao` | `descricao` |
| `value` | `valor` | `valor` |
| `paid` | `pago` | `pago` |
| `paid_at` | `pago_em` | `pagoEm` |
| `default_value` | `valor_padrao` | `valorPadrao` |
| `active` | `ativo` | `ativo` |
| `created_at` | `criado_em` | `criadoEm` |
| `updated_at` | `atualizado_em` | `atualizadoEm` |

Enums:

| Inglês | Português |
| --- | --- |
| `EXPENSE` / `INCOME` | `DESPESA` / `RECEITA` |

Rotas HTTP:

| Inglês | Português |
| --- | --- |
| `POST /api/v1/months` | `POST /api/v1/meses` |
| `GET /api/v1/months/{id}` | `GET /api/v1/meses/{id}` |
| `GET /api/v1/months?reference=YYYY-MM` | `GET /api/v1/meses?referencia=YYYY-MM` |
| `GET /api/v1/months/{id}/summary` | `GET /api/v1/meses/{id}/sumario` |
| `PATCH /api/v1/months/{id}/toggles` | `PATCH /api/v1/meses/{id}/alternancias` |
| `POST /api/v1/months/{monthId}/entries` | `POST /api/v1/meses/{mesId}/lancamentos` |
| `GET /api/v1/months/{monthId}/entries` | `GET /api/v1/meses/{mesId}/lancamentos` |
| `PATCH /api/v1/entries/{id}` | `PATCH /api/v1/lancamentos/{id}` |
| `DELETE /api/v1/entries/{id}` | `DELETE /api/v1/lancamentos/{id}` |
| `POST /api/v1/fixed-expenses` | `POST /api/v1/gastos-fixos` |
| `GET /api/v1/fixed-expenses` | `GET /api/v1/gastos-fixos` |
| `PATCH /api/v1/fixed-expenses/{id}` | `PATCH /api/v1/gastos-fixos/{id}` |
| `DELETE /api/v1/fixed-expenses/{id}` | `DELETE /api/v1/gastos-fixos/{id}` |

Permanece em inglês (escopo técnico): nomes de bibliotecas e frameworks (`Spring Boot`, `Gradle`, `Kotlin`, `PostgreSQL`, `Flyway`, `JPA`, `Jackson`, `Testcontainers`, `AssertJ`, `React`, `Vite`, `TypeScript`, `ESLint`), tipos da JVM e SQL (`BigDecimal`, `NUMERIC`, `JSONB`, `LocalDate`, `UUID`), provedores/protocolos de autenticação (`OAuth`, `OIDC`, `JWT`), anotações Spring/JPA, comandos do shell e do Gradle, paths do build (`dist/`, `build/`, `src/`), sufixos técnicos habituais (`Test`, `Controller`, `Service`, `Repository`, `Request`, `Response`, `Dto`, `Mapper`) e nomes de bibliotecas de teste.

## Consequences

Positivas:
- O vocabulário do código bate com o vocabulário do usuário (`Mês`, `Lançamento`, `Gasto Fixo`, `pago`, `descrição`, `valor`).
- Reduz o custo cognitivo de revisar o código em PR.
- Mensagens de validação, exceções de domínio e textos da UI ganham coerência com o restante do produto.
- Snapshots JSONB do mês, logs e métricas ficam autoexplicativos em português.

Custos e mitigações:
- Toda task subsequente precisa aplicar a tradução nos endpoints, nas migrations, nas entidades e nos payloads da API. A tradução está consolidada na tabela acima e deve ser referenciada nas descrições e critérios de aceite das tasks afetadas.
- Integração com provedores OAuth continua em inglês nas variáveis de configuração do Spring Security (escopo técnico); apenas o que o backend persiste (`sujeito_provedor`) é traduzido.
- Chaves de log e métricas externas (ex.: Prometheus, Datadog) precisam decidir se mantêm o identificador técnico em inglês ou se traduzem. A decisão atual é manter o nome técnico em inglês e usar o termo em português apenas nos logs de aplicação voltados ao usuário.

