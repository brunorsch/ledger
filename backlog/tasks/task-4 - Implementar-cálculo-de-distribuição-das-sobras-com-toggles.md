---
id: TASK-4
title: Implementar cálculo de distribuição das sobras com alternâncias
status: To Do
assignee: []
created_date: '2026-10-11 17:37'
updated_date: '2026-10-11 17:45'
labels:
  - domain
  - backend
dependencies:
  - TASK-3
priority: high
type: task
ordinal: 4000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a regra de distribuição do restante do mês em três categorias: gastos essenciais, lazer e reservas. A regra é o coração do produto descrito em Ledger - Projeto.md, seções "Regra de distribuição das sobras" e "Toggles de investir e de lazer"; nomenclatura em backlog/decisions/decision-1.

Contexto:
- Regra padrão a partir do `Restante` (Receitas − Despesas):
  - `essenciais = min(50% do restante, R$ 1.300,00)`
  - `lazer = min(30% do restante, R$ 700,00)`
  - `reservas = restante − essenciais − lazer`
  - A soma sempre fecha no restante.
- Alternâncias do mês alteram a regra:
  - `investir=false`: redistribui a parte das reservas entre essenciais e lazer na proporção 70/30, mantendo o limite de lazer.
  - `lazer=false`: distribui a parte das reservas entre essenciais e reservas na proporção 50/50, respeitando o limite de gastos.
  - `investir=false && lazer=false`: valor todo em gastos do mês, sem limite.
  - Gastos do mês (essenciais) nunca pode ser desligado.
- Decisão confirmada com o usuário: quando `Restante <= 0`, nenhuma categoria recebe valor e o déficit é exposto.
- Modo de arredondamento: HALF_UP (consistente com `Dinheiro`).
- Os exemplos numéricos do projeto viram casos de teste:
  - Restante `R$ 1.193,08` → essenciais `R$ 596,54`, lazer `R$ 357,92`, reservas `R$ 238,62`.
  - Restante `R$ 7.872,34` → essenciais `R$ 1.300,00`, lazer `R$ 700,00`, reservas `R$ 5.872,34`.

Fora do escopo:
- Persistência (captura dos parâmetros fica em task de migrations).
- Endpoint HTTP (tratado em task de API).
- UI (tratado em task vertical de UI).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Exemplos numéricos do projeto (Restante 1193,08 e 7872,34) produzem os valores esperados com `compareTo` exato
- [ ] #2 `essenciais + lazer + reservas == restante` em todos os cenários válidos com HALF_UP
- [ ] #3 Quando `restante == 0`, as três categorias recebem zero e o déficit exposto é zero
- [ ] #4 Quando `restante < 0`, nenhuma categoria recebe valor e o déficit exposto é igual a `|restante|`
- [ ] #5 `investir=false, lazer=true` mantém limite de lazer e redistribui reservas em 70/30 (essenciais/lazer)
- [ ] #6 `investir=true, lazer=false` redistribui reservas em 50/50 (essenciais/reservas) com limite de gastos
- [ ] #7 `investir=false, lazer=false` aloca o restante todo em essenciais, sem teto
- [ ] #8 Alternâncias inválidas (ex.: essenciais desligado) são rejeitadas pelo domínio antes do cálculo
- [ ] #9 Cálculo é determinístico: mesmas entradas produzem exatamente o mesmo `Dinheiro` em cada execução
- [ ] #10 Cobertura de testes unitários alcança 100% das linhas da função de distribuição
<!-- AC:END -->
