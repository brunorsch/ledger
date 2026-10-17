---
id: TASK-15
title: 'UI: alterar alternâncias de investir e lazer do mês'
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:45'
labels:
  - ui
  - frontend
  - vertical
dependencies:
  - TASK-9
  - TASK-13
priority: high
type: task
ordinal: 15000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar na tela do mês as alternâncias de investir e lazer definidas em "Toggles de investir e de lazer" do projeto (Ledger - Projeto.md; nomenclatura em backlog/decisions/decision-1).

Contexto:
- A alternância de gastos essenciais nunca pode ser desligada (constraint de domínio).
- A alteração das alternâncias reflete imediatamente no bloco de distribuição.
- O backend já entrega `PATCH /api/v1/meses/{id}/alternancias` (criado em task anterior).

Fora do escopo:
- Configuração de percentuais e tetos (esses vivem na snapshot do mês, criada na abertura do mês).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Tela do mês mostra alternâncias de investir e lazer com estado persistido
- [ ] #2 Alternâncias inválidas enviadas pela API são refletidas na UI (sem crash)
- [ ] #3 Alterar uma alternância chama a API e, após sucesso, atualiza o bloco de distribuição sem reload
- [ ] #4 Falha de rede ao alternar mantém o estado anterior e exibe mensagem clara
- [ ] #5 Alternância de essenciais nunca aparece como editável (apenas investir e lazer)
<!-- AC:END -->
