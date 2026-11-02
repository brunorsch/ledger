---
id: TASK-35
title: Calcular o mês atual no momento da navegação no front
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - bug
  - frontend
dependencies: []
references:
  - web/src/layout/AppLayout.tsx
  - web/src/App.tsx
priority: low
type: bug
ordinal: 35000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
O link do Balanço no `AppLayout` e o redirecionamento da rota inicial em `App.tsx` calculam `mesAtual()` uma única vez, quando o módulo carrega. Se o app fica aberto durante a virada do mês, esses atalhos continuam levando ao mês anterior até a página ser recarregada.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Com o app aberto durante a virada do mês, o link do Balanço e a rota inicial levam ao mês corrente sem recarregar a página
- [ ] #2 Os demais usos de mesAtual continuam funcionando como hoje
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Verificado com `pnpm --dir web lint`, `pnpm --dir web exec tsc -b` e teste manual simulando a virada do mês
<!-- DOD:END -->
