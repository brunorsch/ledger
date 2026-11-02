---
id: TASK-28
title: >-
  Corrigir distribuição das sobras com alternâncias desligadas conforme o
  documento
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - bug
  - backend
  - dominio
dependencies:
  - TASK-4
references:
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/domain/DistribuicaoSobras.kt
documentation:
  - >-
    /home/brunorsch/Documents/Obsidian/Projetos/Projetos/Ledger/Ledger -
    Projeto.md
priority: high
type: bug
ordinal: 28000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
A distribuição implementada na TASK-4 diverge da seção "Toggles de investir e de lazer" do `Ledger - Projeto.md`. Com investir desligado, o código divide todo o valor que sobra após os essenciais base usando o percentual de lazer do snapshot. Com lazer desligado, divide esse mesmo valor ao meio entre essenciais e reservas. O documento pede outra regra. Investir desligado redistribui só a parte das reservas, 70/30 entre essenciais e lazer, observando o teto de lazer e ignorando o de essenciais. Lazer desligado divide só a parte do lazer, 50/50 entre essenciais e reservas, respeitando o teto de essenciais, e o excedente escorre para reservas. A divergência aparece quando o restante fica abaixo dos tetos (ex.: restante 1.193,08 com investir desligado dá hoje 1.014,12 / 178,96 / 0,00). O usuário confirmou em 2026-11-01 que vale o documento. A distribuição é calculada na leitura, então a correção também muda o que é exibido nos meses já criados com alguma alternância desligada: confirme com o usuário se isso é aceitável antes de implementar.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Investir desligado e lazer ligado: a parte das reservas da regra base vai 70% para essenciais e 30% para lazer; o lazer respeita o teto de lazer, o excedente vai para essenciais e o teto de essenciais é ignorado
- [ ] #2 Lazer desligado e investir ligado: a parte do lazer da regra base vai 50% para essenciais e 50% para reservas; essenciais respeita o teto de essenciais e o excedente vai para reservas
- [ ] #3 Com percentuais 50%/30% e tetos R$ 1.300/R$ 700 (valores em essenciais / lazer / reservas), restante 1.193,08: investir desligado → 763,57 / 429,51 / 0,00; lazer desligado → 775,50 / 0,00 / 417,58
- [ ] #4 Com os mesmos parâmetros, restante 5.000,00: investir desligado → 4.300,00 / 700,00 / 0,00; lazer desligado → 1.300,00 / 0,00 / 3.700,00
- [ ] #5 As proporções 70/30 e 50/50 são fixas e não dependem dos percentuais do snapshot: com essenciais 50%, lazer 40%, restante 1.000,00 e investir desligado → 570,00 / 430,00 / 0,00
- [ ] #6 Com as duas alternâncias desligadas, todo o restante vai para essenciais, e em todos os casos essenciais + lazer + reservas = restante
- [ ] #7 O endpoint de sumário e distribuição do mês reflete a nova regra (teste de integração)
<!-- AC:END -->
