---
id: TASK-6
title: Criar migrations iniciais do banco com Flyway
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:43'
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
- Os dados históricos de um mês nunca são recalculados a partir de parâmetros atuais: o mês usa a captura gravada nele.
- O banco é PostgreSQL com NUMERIC(14,2) e JSONB.

Entidades e migrations iniciais:
- Tabela `usuarios`: id (uuid), sujeito_provedor (string, único por provedor OAuth — preparado para a task de login, mas nullable no MVP), criado_em.
- Tabela `meses`: id, usuario_id (FK), mes_referencia (date no primeiro dia do mês), captura_parametros (JSONB com `versao_esquema`, percentuais e tetos), alternar_investir (bool), alternar_lazer (bool), deficit (NUMERIC(14,2)), criado_em.
- Tabela `lancamentos`: id, usuario_id (FK), mes_id (FK), tipo (enum: DESPESA|RECEITA), descricao, valor (NUMERIC(14,2)), pago (bool), pago_em (timestamp nullable), criado_em, atualizado_em.
- Tabela `gastos_fixos`: id, usuario_id (FK), descricao, valor_padrao (NUMERIC(14,2)), ativo (bool), criado_em, atualizado_em.

Fora do escopo:
- Camada de repositórios JPA (fica em task própria).
- Endpoints de API (tratados em tasks dedicadas).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Migration V1 cria a tabela `usuarios` com id, sujeito_provedor (nullable) e timestamps
- [ ] #2 Migration V2 cria a tabela `meses` com FK para `usuarios`, captura JSONB contendo `versao_esquema`, percentuais e tetos
- [ ] #3 Migration V3 cria a tabela `lancamentos` com FK para `usuarios` e `meses`, tipo enum, valor NUMERIC(14,2) e flag `pago`
- [ ] #4 Migration V4 cria a tabela `gastos_fixos` com FK para `usuarios`, `valor_padrao` NUMERIC(14,2) e flag `ativo`
- [ ] #5 Migration V5 insere o seed do usuário único do MVP
- [ ] #6 Todas as tabelas de domínio têm índice em `usuario_id`
- [ ] #7 Todas as colunas monetárias são `NUMERIC(14,2)` e jamais `Double`/`Float`
- [ ] #8 Captura JSONB valida que `versao_esquema` é obrigatório
- [ ] #9 Testcontainers aplica o conjunto completo de migrations em PostgreSQL real e os testes de smoke passam
- [ ] #10 Tentativa de aplicar migrations em ordem trocada falha no Testcontainers
<!-- AC:END -->
