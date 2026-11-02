---
id: TASK-14
title: 'UI: criar, editar e remover lançamentos do mês'
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-17 22:33'
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
- [x] #1 Botão "Adicionar despesa" abre formulário com descricao e valor, validando valor > 0 antes de enviar
- [x] #2 Botão "Adicionar receita" abre formulário com descricao e valor, validando valor > 0 antes de enviar
- [x] #3 Editar um lançamento atualiza o sumário e a distribuição visíveis na tela após sucesso
- [x] #4 Remover um lançamento pede confirmação antes de chamar o endpoint e atualiza a tela após sucesso
- [x] #5 Falha de validação na API é exibida como mensagem inline no formulário
- [x] #6 Layout funciona em viewport mobile (>= 360px) e desktop (>= 1024px)
- [x] #7 Toggle rápido de pago na listagem: tocar no status de uma despesa alterna pago via PATCH e atualiza o sumário e a distribuição imediatamente (consistência verificável manualmente)
- [x] #8 Formulários de criar/editar despesa não expõem o campo pago (a flag só muda pelo toggle da listagem)
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Instalar @tanstack/react-query (aprovado pelo usuário); sem Zustand.
2. Camada API (web/src/api): generalizar requisicao (POST/PATCH/DELETE, body JSON, Content-Type, 204 sem corpo, parsear {"mensagem"} em ErroApi); novas funções criarLancamento/alterarLancamento/alternarPagoLancamento/removerLancamento; hooks useQuery/useMutation com invalidação de sumario+lancamentos no sucesso; QueryClientProvider em main.tsx.
3. UI (@designer): componentes base (Modal, Botao, CampoTexto, CampoValor com validação >0) e lancamentos/ (FormularioLancamento criar/editar sem campo pago, ConfirmacaoRemocao, toggle rápido de pago no item); botões Adicionar despesa/receita no App; estado de diálogo local (useState).
4. Responsivo 360px–1024px; sem mudanças no backend; sem testes de frontend; validação manual com backend local.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verificação funcional via navegador (dev server :9002, backend local :9100): criar despesa (12,34) e receita (10,00) com validação client-side inline; editar valor (12,34→50,00) recalculou sumário e distribuição imediatamente; toggle pago mudou 'A pagar' 1.062,84→1.050,50; remoção com confirmação (cancelar preserva, confirmar remove e atualiza tela); erro 400 da API exibido inline no form (mensagem 'valor deve ser maior que zero: 0.00' via role=alert, modal permanece aberto). ErroApi parseia {"mensagem"} do body. Correções de integração: toggle usa PATCH /lancamentos/{id} (não /pago, que não existe), requisicao trata 204 sem corpo, tela de erro não pisca entre cargas (condição carregando), catch no toggle evita rejeição não tratada. Checks: pnpm exec tsc --noEmit, pnpm build, pnpm lint OK. Sem mudanças no backend. AC #6 pendente: mobile 360px sem ferramenta de emulação no browser — aguarda conferência manual.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
UI de CRUD de lançamentos na tela do mês: botões Adicionar despesa/receita com formulário (descricao+valor, validação >0 client-side), edição e remoção (com confirmação), toggle rápido de pago via PATCH. Estado de servidor com TanStack Query (useQuery/useMutation com invalidação de sumario+lancamentos no sucesso — tela atualiza imediatamente), camada api/cliente generalizada (POST/PATCH/DELETE, 204, mensagens {"mensagem"} parseadas em ErroApi). Componentes base (Modal, Botao, CampoTexto, CampoValor) e de lançamentos criados. Verificado em navegador real contra backend local (criar/editar/toggle/remover + erro 400 inline); tsc/build/lint OK; revisão visual desktop sem defeitos. AC #6 (mobile 360px) aguarda conferência manual do usuário.
<!-- SECTION:FINAL_SUMMARY:END -->
