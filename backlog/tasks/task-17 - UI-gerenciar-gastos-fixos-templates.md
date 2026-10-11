---
id: TASK-17
title: 'UI: gerenciar gastos fixos (templates)'
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:44'
labels:
  - ui
  - frontend
  - vertical
dependencies:
  - TASK-2
  - TASK-11
priority: high
type: task
ordinal: 17000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a tela de gestão dos gastos fixos usados como modelo para os meses novos (Ledger - Projeto.md, seção "Como o passado não muda"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- O backend já entrega o CRUD de gastos fixos (criado em task anterior).
- A tela mostra a lista de gastos fixos ativos e permite criar, editar e remover.
- Mudanças aqui só valem para os meses criados depois da alteração (essa garantia é responsabilidade do backend e é coberta por testes integrados; a UI precisa apenas refletir corretamente).

Fora do escopo:
- Reaplicação retroativa em meses já criados.
- Importação em massa.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Tela lista gastos fixos ativos do usuário com descricao e valor em BRL
- [ ] #2 Botão "Adicionar gasto fixo" abre formulário validando valor > 0
- [ ] #3 Editar um gasto fixo atualiza a lista sem reload completo
- [ ] #4 Remover um gasto fixo pede confirmação antes de chamar o endpoint
- [ ] #5 Tela funciona em viewport mobile e desktop
<!-- AC:END -->
