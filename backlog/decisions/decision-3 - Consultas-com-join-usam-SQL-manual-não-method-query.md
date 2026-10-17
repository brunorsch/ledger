---
id: decision-3
title: 'Consultas com join usam SQL manual, não method query'
date: '2026-10-17 01:22'
status: accepted
---
## Context

Telas de leitura do Ledger usam SQL ou projeção, sem montar agregados (AGENTS.md). O Spring Data JDBC oferece method queries derivadas (`findAllBy...`), convenientes para leituras simples, mas quando a consulta envolve mais de uma tabela (join), a method query derivada não expressa bem nem o SQL nem o mapeamento de linhas.

## Decision

Consultas que envolvem join entre tabelas são escritas com SQL manual (JdbcTemplate ou `@Query` com RowMapper próprio), não com method query derivada. Method query derivada fica restrita a leituras simples de uma única tabela, filtrando por colunas do próprio agregado.

## Consequences

- O SQL de join fica explícito e revisável, e o mapeamento de linhas fica junto da consulta que o alimenta.
- Method queries derivadas continuam permitidas para leituras single-table, onde a geração automática é clara e segura.

