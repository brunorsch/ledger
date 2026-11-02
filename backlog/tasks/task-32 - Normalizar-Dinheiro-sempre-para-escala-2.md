---
id: TASK-32
title: Normalizar Dinheiro sempre para escala 2
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - bug
  - backend
  - dominio
dependencies: []
references:
  - server/src/main/kotlin/br/dev/brunorsch/ledger/server/domain/Dinheiro.kt
documentation:
  - >-
    /home/brunorsch/Documents/Obsidian/Projetos/Projetos/Ledger/Ledger -
    Projeto.md
priority: medium
type: bug
ordinal: 32000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
O `Ledger - Projeto.md` define que o dinheiro tem sempre escala 2 e que o tipo normaliza a escala na criação. `Dinheiro.normalizar` só arredonda quando a escala passa de 2, então `Dinheiro("10")` e `Dinheiro("10.5")` ficam com escala 0 e 1. Com isso, dois valores iguais podem ser diferentes no `equals`, e o formato do valor depende de onde ele veio.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Todo Dinheiro criado tem escala 2: "10" → 10.00, "10.5" → 10.50 e "10.005" → 10.01 (HALF_UP)
- [ ] #2 Valores negativos criados com allowNegative seguem a mesma normalização
- [ ] #3 Dinheiro("10") e Dinheiro("10.00") são iguais
- [ ] #4 Valores monetários nas respostas da API sempre têm duas casas decimais
- [ ] #5 Testes unitários cobrem escala menor, igual e maior que 2
<!-- AC:END -->
