---
id: TASK-12
title: Gerar lançamentos a partir de gastos fixos na criação do mês
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:44'
labels:
  - service
  - backend
dependencies:
  - TASK-7
  - TASK-10
  - TASK-11
priority: high
type: task
ordinal: 12000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a regra que, ao criar um mês novo, gera lançamentos de despesa (pago=false) a partir dos gastos fixos ativos do usuário (Ledger - Projeto.md, seção "Como o passado não muda"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Cada mês guarda seus próprios lançamentos concretos; os gastos fixos são um modelo que só gera cópias quando um mês novo é criado.
- Mudanças em gastos fixos não afetam lançamentos de meses já criados.
- A captura de parâmetros (incluindo o conjunto de gastos fixos usados como origem) é capturada no momento da criação do mês.
- Lançamentos gerados nascem com pago=false e descricao/valor copiados do gasto fixo; o usuário pode editá-los livremente depois.

Fora do escopo:
- Alternâncias (ficam na criação do mês, alteráveis depois em task dedicada).
- Captura dos parâmetros em si (já tratado na task de migrations).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Criar mês sem gastos fixos não cria nenhum lançamento automático
- [ ] #2 Criar mês com N gastos fixos ativos cria N lançamentos DESPESA com pago=false no mês novo
- [ ] #3 Lançamento gerado tem descricao e valor iguais ao gasto fixo de origem
- [ ] #4 Editar um gasto fixo depois da criação do mês não altera os lançamentos já gerados (teste integrado)
- [ ] #5 Adicionar um gasto fixo depois da criação do mês não insere lançamentos no mês já criado (teste integrado)
- [ ] #6 Marcar um gasto fixo como ativo=false antes da criação do mês impede que ele gere lançamento
- [ ] #7 A geração é idempotente: chamar a rotina duas vezes para o mesmo mês não duplica lançamentos
<!-- AC:END -->
