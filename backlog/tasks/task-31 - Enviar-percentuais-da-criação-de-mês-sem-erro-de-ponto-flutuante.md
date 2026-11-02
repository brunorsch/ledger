---
id: TASK-31
title: Enviar percentuais da criação de mês sem erro de ponto flutuante
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - bug
  - frontend
dependencies: []
references:
  - web/src/screen/BalancoScreen/components/FormularioCriarMes.tsx
priority: medium
type: bug
ordinal: 31000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
O `FormularioCriarMes` divide por 100, como `number`, o percentual digitado antes de enviar à API. 33,3% vira 0.33299999999999996 e o backend grava esse valor no snapshot; com restante 15,00, os essenciais saem 4,99 em vez de 5,00. O erro fica gravado no mês para sempre, porque o snapshot não muda. O AGENTS.md proíbe aritmética de dinheiro no front, e o mesmo cuidado vale para os percentuais que entram no cálculo.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Um percentual com até uma casa decimal chega à API como o valor digitado dividido por 100, sem resíduo (33,3% → 0.333; 50% → 0.5)
- [ ] #2 Um mês criado pelo front com essenciais em 33,3% e restante 15,00 mostra essenciais R$ 5,00
- [ ] #3 O pré-preenchimento a partir de um mês existente continua exibindo o percentual gravado sem resíduo (0.333 → 33,3)
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Verificado com `pnpm --dir web lint`, `pnpm --dir web exec tsc -b` e teste manual (o projeto não tem testes de front)
<!-- DOD:END -->
