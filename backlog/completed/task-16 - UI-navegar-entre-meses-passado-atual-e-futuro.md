---
id: TASK-16
title: 'UI: navegar entre meses (passado, atual e futuro)'
status: Done
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-18 07:28'
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
- [x] #1 Cabeçalho da tela mostra o mês corrente (YYYY-MM) e setas para anterior e próximo
- [x] #2 Clicar em "anterior" navega para o mês imediatamente anterior e carrega seus dados
- [x] #3 Clicar em "próximo" navega para o mês imediatamente seguinte
- [x] #4 Tecla de atalho ou swipe horizontal retorna ao mês corrente (definição no README da task)
- [x] #5 Seletor de mês (date picker ou input YYYY-MM) permite pular para qualquer mês
- [x] #6 Estado de navegação persiste em URL usando `/meses/{referencia}` (deep linking)
- [x] #7 Tela funciona em viewport mobile e desktop
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
8. Fluxo de mês inexistente (decisão do usuário): tela própria com botão 'Criar mês'; formulário herda parâmetros do último mês criado para confirmação; novo endpoint GET /api/v1/meses/ultimo no backend
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Navegação para mês inexistente: GET /meses?referencia retorna 404 (backend não cria no GET). Decisão do usuário: tela dedicada com 'Criar mês', formulário pré-preenchido com parâmetros do último mês.

Validação no navegador (stack local, 2026-10-18): setas anterior/próximo, seletor type=month, Ctrl+H, botão 'Ir para Mês Atual', deep link /meses/{referencia} e redirecionamento de referência inválida exercitados. Fluxo de mês inexistente (tela 'Este mês ainda não foi criado' + POST /meses com parâmetros herdados do último mês) verificado criando 2025-03. pnpm build e lint ok. Backend: endpoint GET /api/v1/meses/ultimo com testes de integração (12 testes, 0 falhas).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Navegação entre meses com react-router (rota /meses/:referencia como fonte da verdade): setas anterior/próximo, seletor type=month, Ctrl+H e botão 'Ir para Mês Atual', deep linking e redirect de referências inválidas. Fluxo para mês inexistente: tela dedicada com 'Criar mês' e formulário herdando parâmetros do último mês (novo endpoint GET /api/v1/meses/ultimo com testes). Shell de navegação: sidebar desktop + bottom bar mobile, com tela renomeada para 'Balanço'. Verificado no navegador com o stack local, pnpm build/lint e ./gradlew :server:test (12 testes).
<!-- SECTION:FINAL_SUMMARY:END -->
