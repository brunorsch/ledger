---
id: TASK-6
title: Criar migrations iniciais do banco com Flyway
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-13 04:45'
labels:
  - persistence
  - backend
dependencies:
  - TASK-1
priority: high
type: task
ordinal: 6000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Criar o schema inicial do banco de dados do Ledger com Flyway, atendendo aos requisitos de `usuario_id` desde o primeiro dia e ao snapshot de parâmetros por mês (Ledger - Projeto.md, seções "Segurança e multiusuário", "Dados" e "Stack e entrega"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Toda tabela de dados do usuário precisa de `usuario_id` desde o primeiro dia, mesmo com usuário único.
- O snapshot de parâmetros do mês (percentuais, tetos e demais parâmetros gravados na criação) tem uma versão de formato `versao_esquema`, porque ele vive para sempre enquanto o código muda.
- Migrações são versionadas desde o primeiro dia; o histórico do banco precisa ser reproduzível.
- Os dados históricos de um mês nunca são recalculados a partir de parâmetros atuais: o mês usa a snapshot gravada nele.
- O banco é PostgreSQL com NUMERIC(14,2) e JSONB.

Entidades e migrations iniciais:
- Tabela `usuarios`: id (uuid), sujeito_provedor (string, único por provedor OAuth — preparado para a task de login, mas nullable no MVP), criado_em.
- Tabela `meses`: id, usuario_id (FK), mes_referencia (date no primeiro dia do mês), snapshot_parametros (JSONB com `versao_esquema`, percentuais e tetos), alternar_investir (bool), alternar_lazer (bool), deficit (NUMERIC(14,2)), criado_em.
- Tabela `lancamentos`: id, usuario_id (FK), mes_id (FK), tipo (enum: DESPESA|RECEITA), descricao, valor (NUMERIC(14,2)), pago (bool), pago_em (timestamp nullable), criado_em, atualizado_em.
- Tabela `gastos_fixos`: id, usuario_id (FK), descricao, valor_padrao (NUMERIC(14,2)), ativo (bool), criado_em, atualizado_em.

Fora do escopo:
- Camada de repositórios JPA (fica em task própria).
- Endpoints de API (tratados em tasks dedicadas).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Migration V1 cria a tabela `usuarios` com id, sujeito_provedor (nullable) e timestamps
- [x] #2 Migration V2 cria a tabela `meses` com FK para `usuarios`, snapshot JSONB contendo `versao_esquema`, percentuais e tetos
- [x] #3 Migration V3 cria a tabela `lancamentos` com FK para `usuarios` e `meses`, tipo enum, valor NUMERIC(14,2) e flag `pago`
- [x] #4 Migration V4 cria a tabela `gastos_fixos` com FK para `usuarios`, `valor_padrao` NUMERIC(14,2) e flag `ativo`
- [x] #5 Migration V5 insere o seed do usuário único do MVP
- [x] #6 Todas as tabelas de domínio têm índice em `usuario_id`
- [x] #7 Todas as colunas monetárias são `NUMERIC(14,2)` e jamais `Double`/`Float`
- [x] #8 snapshot JSONB valida que `versao_esquema` é obrigatório
- [x] #9 Testcontainers aplica o conjunto completo de migrations em PostgreSQL real e os testes de smoke passam
- [ ] #10 Tentativa de aplicar migrations em ordem trocada falha no Testcontainers
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Migrations V1-V5 em server/src/main/resources/db/migration: id interno bigint (identity) em todas as tabelas; id_publico uuid (único, gerado pela aplicação) nas entidades expostas ao front (meses, lancamentos, gastos_fixos); FKs por id interno; seed do usuário único.
2. Adicionar instrução no AGENTS.md sobre ids internos bigint vs id_publico uuid.
3. Adicionar dependências de teste (spring-boot-testcontainers, testcontainers junit-jupiter e postgresql) e base de teste com PostgreSQLContainer postgres:17 + @ServiceConnection.
4. Teste de smoke integrado (AC #1-#8): tabelas, colunas, FKs, enum, índices usuario_id, colunas NUMERIC(14,2), seed e CHECK do versao_esquema no JSONB.
5. Teste de fora de ordem (AC #10): Flyway puro em banco separado no mesmo container; remove V3 do history e espera FlywayException.
6. Corrigir LedgerApplicationTests (quebrado hoje por falta de datasource) para usar o container.
7. Rodar ./gradlew :server:test até ficar verde.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test -> BUILD SUCCESSFUL, 55 testes (0 falhas/erros) em PostgreSQL real via Testcontainers (postgres:17). Descoberta relevante para tasks futuras: Spring Boot 4.1.1 fixa Testcontainers 2.0.5, cujos módulos foram renomeados para testcontainers-junit-jupiter e testcontainers-postgresql (o BOM do Boot não importa o BOM do Testcontainers, então a versão foi declarada explicitamente). O container é um singleton iniciado no initializer do companion (SuportePostgres) para evitar que o Testcontainers reinicie o container com porta nova e invalide o contexto Spring cacheado.

Decisão do usuário: removido MigracoesForaDeOrdemTest — não testamos o comportamento interno do Flyway (detecção de fora de ordem); confiamos que a dependência valida os próprios comportamentos. AC #10 desmarcado por não haver mais teste cobrindo-o. Smoke tests do schema (MigracoesPostgresTest) permanecem.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Criadas migrations Flyway V1-V5 (usuarios, meses, lancamentos, gastos_fixos e seed do usuário único), com id interno bigint identity e id_publico uuid nas entidades expostas ao front (conforme ajuste acordado e documentado no AGENTS.md). Adicionada infraestrutura Testcontainers (SuportePostgres com PostgreSQLContainer postgres:17 + @ServiceConnection), corrigido LedgerApplicationTests e criado MigracoesPostgresTest com smoke do schema (ACs #1-#8). Verificado com ./gradlew :server:test: 54 testes verdes.
<!-- SECTION:FINAL_SUMMARY:END -->
