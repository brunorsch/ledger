---
id: TASK-8
title: API para obter sumário e distribuição de um mês
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-17 01:15'
labels:
  - api
  - backend
dependencies:
  - TASK-3
  - TASK-4
  - TASK-5
  - TASK-7
priority: high
type: task
ordinal: 8000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Expor endpoint REST que retorna o sumário e a distribuição das sobras de um mês (Ledger - Projeto.md, seções "Definições do sumário do mês" e "Toggles de investir e de lazer"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- O sumário é derivado dos lançamentos do próprio mês.
- A distribuição das sobras usa a snapshot de parâmetros gravada no mês (nunca os parâmetros globais atuais).
- Alternâncias do mês controlam o comportamento da distribuição.
- Restante <= 0 não aloca nada nas três categorias e expõe o déficit (decisão confirmada com o usuário).

Endpoint:
- `GET /api/v1/meses/{id}/sumario` — retorna Receitas, Despesas, A pagar, Restante, Distribuição (essenciais, lazer, reservas) e déficit.

Fora do escopo:
- Edição de alternâncias (task dedicada).
- Lançamentos (CRUD em task dedicada).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Endpoint retorna 200 com sumário e distribuição derivados dos lançamentos e da snapshot do mês
- [x] #2 Mês sem lançamentos retorna `Receitas = Despesas = A pagar = Restante = 0`
- [x] #3 Quando `Restante > 0`, distribuição aplica as alternâncias gravadas no mês
- [x] #4 Quando `Restante <= 0`, as três categorias retornam zero e o déficit exposto é igual a `|Restante|`
- [x] #5 Alterar um lançamento do mês reflete imediatamente em nova chamada ao endpoint (consistência verificável em teste integrado)
- [x] #6 Alternâncias do mês lidas da snapshot, não de configuração global (alterar parâmetros globais não afeta o resultado do endpoint)
- [x] #7 Teste integrado cobre caminho feliz e o caso de `Restante <= 0`
- [x] #8 Dinheiro é serializado como string no JSON de resposta
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Dominio: DistribuicaoSobras.calcular passa a receber ParametrosCalculo (percentuais e tetos do snapshot gravado no mes) em vez das constantes fixas 50/30/1300/700; comportamento preservado para o snapshot padrao. 2. Atualizar DistribuicaoSobrasTest para passar o snapshot padrao e adicionar teste que comprova o uso dos parametros informados. 3. Leitura de lancamentos com method query: entidade LancamentoRegistro (@Table lancamentos) com enum TipoLancamento e conversor de leitura do enum PG no JdbcConfig; LancamentoRepository (Spring Data) com findAllByMesIdAndUsuarioIdOrderById + listarPorMes que converte para o dominio via paraDominio(). 4. MesService.obterSumario(idPublico, usuarioId): busca mes (404 se ausente), le lancamentos do mes, monta Sumario + DistribuicaoSobras com o snapshot de parametros e as alternancias do mes e devolve MesSumarioResponse. 5. Controller: GET /api/v1/meses/{id}/sumario com header X-Usuario-Id. 6. Teste integrado MesSumarioApiTest (MockMvc + Testcontainers) cobrindo os ACs: caminho feliz com strings de dinheiro, mes sem lancamentos, restante <= 0 com deficit, alternancias do mes, consistencia apos alterar lancamento, snapshots diferentes por mes, 404.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test -> BUILD SUCCESSFUL, 75 testes (0 falhas) em PostgreSQL real via Testcontainers, incluindo 8 novos em MesSumarioApiTest. JaCoCo: pacote domain com 0 linhas perdidas em todas as classes (DistribuicaoSobras.Companion 0/48 linhas perdidas). Decisões: DistribuicaoSobras.calcular agora recebe ParametrosCalculo (percentuais e tetos do snapshot do mês) — com o snapshot padrão os resultados são idênticos aos de antes; os splits 70/30 e 50/50 das alternâncias permanecem como aprovado na TASK-4, com a fatia de lazer usando o percentuais.lazer do snapshot. Lançamentos lidos via SQL (JdbcTemplate) em LancamentoRepository, sem montar agregado; CRUD continua fora do escopo (TASK-10), então os testes inserem/alteram lançamentos via SQL. Terminologia: 'snapshot' no lugar de 'captura' (decision-2).

Review aplicado: (1) MesSumarioResponse usa fromDomain no lugar de 'de' (mais claro); (2) LancamentoRepository deixou o JdbcTemplate e passou a usar method query do Spring Data JDBC — entidade LancamentoRegistro (@Table lancamentos, colunas usadas na leitura) com enum TipoLancamento, conversor de leitura PGobject->TipoLancamento no JdbcConfig (a escrita entra com o CRUD, TASK-10) e findAllByMesIdAndUsuarioIdOrderById + listarPorMes (default method) que converte para o dominio via paraDominio(). Validacao: ./gradlew :server:test -> 75 testes (0 falhas); JaCoCo dominio com 0 linhas perdidas.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Endpoint GET /api/v1/meses/{id}/sumario com escopo por usuario_id (header X-Usuario-Id): retorna receitas, despesas, aPagar, restante e distribuicao (essenciais, lazer, reservas, deficit), tudo derivado dos lançamentos do mês e do snapshot de parâmetros e alternâncias gravados nele. DistribuicaoSobras.calcular passou a receber ParametrosCalculo (percentuais e tetos do snapshot, nunca valores globais); comportamento preservado para o snapshot padrão. Lançamentos lidos via SQL em LancamentoRepository (tela de leitura, sem agregado). DTO MesSumarioResponse com Dinheiro serializado como string. Verificado com ./gradlew :server:test (75 testes, 0 falhas, Testcontainers postgres:17): os 8 ACs cobertos em MesSumarioApiTest (feliz, mês vazio, restante <= 0 com déficit, alternâncias do mês, consistência após alterar lançamento, snapshots diferentes por mês, 404) e DistribuicaoSobrasTest (16 testes, snapshot parametrizado). JaCoCo: domínio com 100% das linhas cobertas.
<!-- SECTION:FINAL_SUMMARY:END -->
