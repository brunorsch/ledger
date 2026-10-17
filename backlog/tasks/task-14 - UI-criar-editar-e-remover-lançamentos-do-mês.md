---
id: TASK-14
title: 'UI: criar, editar e remover lançamentos do mês'
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-17 21:42'
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
- O backend já entrega POST/PATCH/DELETE /api/v1/meses/{mesId}/lancamentos... (criado em task anterior).
- A flag pago de uma despesa NÃO é editável nos formulários: é alternada por um toggle rápido direto na listagem (decisão do usuário em 2026-10-17), via PATCH.
- A UI trata entradas inválidas no cliente (valor vazio, valor <= 0) antes de chamar a API.
- A edição e o toggle refletem imediatamente no sumário e na distribuição exibidos na mesma tela (consistência verificável manualmente).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Botão "Adicionar despesa" abre formulário com descricao e valor, validando valor > 0 antes de enviar
- [ ] #2 Botão "Adicionar receita" abre formulário com descricao e valor, validando valor > 0 antes de enviar
- [ ] #3 Editar um lançamento atualiza o sumário e a distribuição visíveis na tela após sucesso
- [ ] #4 Remover um lançamento pede confirmação antes de chamar o endpoint e atualiza a tela após sucesso
- [ ] #5 Falha de validação na API é exibida como mensagem inline no formulário
- [ ] #6 Layout funciona em viewport mobile (>= 360px) e desktop (>= 1024px)
- [ ] #7 Toggle rápido de pago na listagem: tocar no status de uma despesa alterna pago via PATCH e atualiza o sumário e a distribuição imediatamente (consistência verificável manualmente)
- [ ] #8 Formulários de criar/editar despesa não expõem o campo pago (a flag só muda pelo toggle da listagem)
<!-- AC:END -->
