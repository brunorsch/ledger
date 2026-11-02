---
id: TASK-33
title: Pré-preencher os tetos do documento ao criar o primeiro mês
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - frontend
dependencies: []
references:
  - web/src/screen/BalancoScreen/index.tsx
documentation:
  - >-
    /home/brunorsch/Documents/Obsidian/Projetos/Projetos/Ledger/Ledger -
    Projeto.md
priority: medium
type: bug
ordinal: 33000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Quando ainda não existe nenhum mês, o formulário de criar mês vem com percentuais de 50% e 30%, mas com tetos de R$ 0,00. Se o usuário não perceber, essenciais e lazer ficam zerados e todo o restante vai para reservas, e o snapshot não pode ser corrigido depois. O `Ledger - Projeto.md` define tetos de R$ 1.300 para gastos e R$ 700 para lazer. Decisão do usuário (2026-11-01): usar esses valores como padrão do primeiro mês. O pré-preenchimento a partir de um mês existente não muda.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Sem nenhum mês criado, o formulário de criar mês abre com essenciais 50%, lazer 30%, teto de essenciais R$ 1.300,00 e teto de lazer R$ 700,00
- [ ] #2 Com algum mês já criado, o formulário continua pré-preenchido a partir dele, como hoje
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Verificado com `pnpm --dir web lint`, `pnpm --dir web exec tsc -b` e teste manual (o projeto não tem testes de front)
<!-- DOD:END -->
