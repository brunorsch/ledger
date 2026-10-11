---
id: TASK-14
title: 'UI: criar, editar e remover lançamentos do mês'
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:44'
labels:
  - ui
  - frontend
  - vertical
dependencies:
  - TASK-10
  - TASK-13
priority: high
type: task
ordinal: 14000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a criação, edição e remoção de lançamentos a partir da tela do mês (Ledger - Projeto.md, seção "A experiencia basica precisa ser mantida"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Lançamentos têm: tipo (DESPESA ou RECEITA), descricao, valor e status pago (apenas para despesa).
- O backend já entrega `POST/PATCH/DELETE /api/v1/meses/{mesId}/lancamentos...` (criado em task anterior).
- A UI trata entradas inválidas no cliente (valor vazio, valor <= 0) antes de chamar a API.
- A edição reflete imediatamente no sumário e na distribuição exibidos na mesma tela (consistência verificável manualmente).

Fora do escopo:
- Edição em massa ou importação.
- Categoria, anotações, dia de vencimento, forma de pagamento (fora do MVP).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Botão "Adicionar despesa" abre formulário com descricao e valor, validando valor > 0 antes de enviar
- [ ] #2 Botão "Adicionar receita" abre formulário com descricao e valor, validando valor > 0 antes de enviar
- [ ] #3 Checkbox "Pago" aparece apenas no formulário de despesa e é enviado como `pago=true` quando marcado
- [ ] #4 Editar um lançamento atualiza o sumário e a distribuição visíveis na tela após sucesso
- [ ] #5 Remover um lançamento pede confirmação antes de chamar o endpoint e atualiza a tela após sucesso
- [ ] #6 Falha de validação na API é exibida como mensagem inline no formulário
- [ ] #7 Layout funciona em viewport mobile (>= 360px) e desktop (>= 1024px)
<!-- AC:END -->
