---
id: TASK-8
title: API para obter sumário e distribuição de um mês
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:43'
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
- A distribuição das sobras usa a captura de parâmetros gravada no mês (nunca os parâmetros globais atuais).
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
- [ ] #1 Endpoint retorna 200 com sumário e distribuição derivados dos lançamentos e da captura do mês
- [ ] #2 Mês sem lançamentos retorna `Receitas = Despesas = A pagar = Restante = 0`
- [ ] #3 Quando `Restante > 0`, distribuição aplica as alternâncias gravadas no mês
- [ ] #4 Quando `Restante <= 0`, as três categorias retornam zero e o déficit exposto é igual a `|Restante|`
- [ ] #5 Alterar um lançamento do mês reflete imediatamente em nova chamada ao endpoint (consistência verificável em teste integrado)
- [ ] #6 Alternâncias do mês lidas da captura, não de configuração global (alterar parâmetros globais não afeta o resultado do endpoint)
- [ ] #7 Teste integrado cobre caminho feliz e o caso de `Restante <= 0`
- [ ] #8 Dinheiro é serializado como string no JSON de resposta
<!-- AC:END -->
