---
id: TASK-17
title: 'UI: gerenciar gastos fixos (templates)'
status: Done
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-18 07:28'
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
- [x] #1 Tela lista gastos fixos ativos do usuário com descricao e valor em BRL
- [x] #2 Botão "Adicionar gasto fixo" abre formulário validando valor > 0
- [x] #3 Editar um gasto fixo atualiza a lista sem reload completo
- [x] #4 Remover um gasto fixo pede confirmação antes de chamar o endpoint
- [x] #5 Tela funciona em viewport mobile e desktop
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação no navegador (stack local, 2026-10-18): lista de ativos com BRL, criação com validação valor > 0 (0,00 rejeitado com mensagem), edição sem reload (R$ 120,50 -> R$ 130,00 via invalidação TanStack Query) e remoção com modal de confirmação exercitados. pnpm build e lint ok.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Tela de gastos fixos (/gastos-fixos): lista de ativos com descrição e valor em BRL, criação com validação valor > 0, edição sem reload completo (invalidação TanStack Query) e remoção com confirmação. Cliente/queries no padrão existente. Verificado no navegador com o stack local, pnpm build/lint.
<!-- SECTION:FINAL_SUMMARY:END -->
