---
id: TASK-13
title: 'UI: visualizar mês com lançamentos, sumário e distribuição'
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
  - TASK-8
  - TASK-10
priority: high
type: task
ordinal: 13000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a tela principal de visualização do mês no Ledger, equivalente ao cabeçalho da planilha (Ledger - Projeto.md, seção "A experiencia basica precisa ser mantida"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- A tela mostra três blocos: lista de lançamentos do mês (despesas e receitas), sumário (Receitas, Despesas, A pagar, Restante) e distribuição das sobras (essenciais, lazer, reservas e déficit quando aplicável).
- A formatação monetária é BRL e nunca é calculada no frontend — o backend entrega strings como `"1193.08"` (campo `valor` da entidade Lançamento).
- O detalhe de cada lançamento (categoria, anotações, forma de pagamento) é opcional e fica fora da visualização principal (regra "Manter simples").
- O backend já entrega os dados via `GET /api/v1/meses/{id}/sumario` e `GET /api/v1/meses/{mesId}/lancamentos` (criados em tasks anteriores).

Fora do escopo:
- Edição de lançamentos (task dedicada).
- Alternâncias de investir/lazer (task dedicada).
- Navegação entre meses (task dedicada).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Tela carrega lançamentos e sumário do mês selecionado e exibe os três blocos
- [ ] #2 Cada lançamento mostra descricao, valor formatado em BRL e status pago/não pago (quando aplicável)
- [ ] #3 Bloco de sumário mostra Receitas, Despesas, A pagar e Restante formatados em BRL
- [ ] #4 Bloco de distribuição mostra essenciais, lazer, reservas e déficit (quando Restante <= 0) formatados em BRL
- [ ] #5 Loading state aparece enquanto as requests estão em voo
- [ ] #6 Erro de rede é exibido com mensagem clara e opção de tentar novamente
- [ ] #7 Layout é responsivo e usável em viewport mobile (>= 360px) e desktop (>= 1024px)
- [ ] #8 Página responde em menos de 200ms para a interação após o carregamento inicial
<!-- AC:END -->
