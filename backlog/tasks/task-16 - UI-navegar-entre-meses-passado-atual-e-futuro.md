---
id: TASK-16
title: 'UI: navegar entre meses (passado, atual e futuro)'
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:44'
labels:
  - ui
  - frontend
dependencies:
  - TASK-13
priority: high
type: task
ordinal: 16000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a navegação entre meses mantendo a experiência centrada no mês (Ledger - Projeto.md, seção "Experiência centrada no mês"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- O usuário precisa navegar entre passado e futuro com poucos cliques.
- O "mês corrente" é resolvido no backend ou no cliente a partir do relógio; ambos os caminhos precisam ser consistentes.
- Cada mês é independente; mexer no passado não afeta o futuro.
- O seletor e a URL expõem o parâmetro `referencia` no formato YYYY-MM.

Fora do escopo:
- Listagem anual com colagem de meses (a planilha tinha 4 meses lado a lado; aqui é por mês).
- Filtros por status ou categoria.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Cabeçalho da tela mostra o mês corrente (YYYY-MM) e setas para anterior e próximo
- [ ] #2 Clicar em "anterior" navega para o mês imediatamente anterior e carrega seus dados
- [ ] #3 Clicar em "próximo" navega para o mês imediatamente seguinte
- [ ] #4 Tecla de atalho ou swipe horizontal retorna ao mês corrente (definição no README da task)
- [ ] #5 Seletor de mês (date picker ou input YYYY-MM) permite pular para qualquer mês
- [ ] #6 Estado de navegação persiste em URL usando `/meses/{referencia}` (deep linking)
- [ ] #7 Tela funciona em viewport mobile e desktop
<!-- AC:END -->
