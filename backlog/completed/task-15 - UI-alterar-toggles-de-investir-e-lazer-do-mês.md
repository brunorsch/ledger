---
id: TASK-15
title: 'UI: alterar alternâncias de investir e lazer do mês'
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-18 04:58'
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
- [x] #1 Tela do mês mostra alternâncias de investir e lazer com estado persistido
- [x] #2 Alternâncias inválidas enviadas pela API são refletidas na UI (sem crash)
- [x] #3 Alterar uma alternância chama a API e, após sucesso, atualiza o bloco de distribuição sem reload
- [x] #4 Falha de rede ao alternar mantém o estado anterior e exibe mensagem clara
- [x] #5 Alternância de essenciais nunca aparece como editável (apenas investir e lazer)
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Camada API: AlterarAlternanciasCorpo/AlternanciasResponse em tipos.ts; alterarAlternancias em cliente.ts (PATCH /api/v1/meses/{id}/alternancias); useAlterarAlternancias em consultas.ts invalidando sumario(mesId) + prefixo ['mes'] no sucesso.
2. UI (@designer): bloco 'Configurações' no aside (nomenclatura de UI aprovada pelo usuário; 'alternancias' segue termo de negócio no código) com toggles Investir e Lazer lidos de mes.alternarInvestir/alternarLazer; sem toggle de essenciais; toggle desabilitado durante a mutação; falha mantém estado anterior + mensagem inline; renderização defensiva de payload inesperado (AC #2).
3. Sem mudanças no backend; sem testes de frontend; verificação tsc/build/lint + navegador real (toggle → distribuição atualiza; falha simulada → estado anterior + mensagem).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verificação funcional em navegador real (dev :9000, backend local :9100): bloco 'Configurações' com switches Investir/Lazer lendo estado persistido do mes; toggle Investir→off mudou distribuição para essenciais 3.102,07/lazer 547,43/reservas 0 (regra investir=false+lazer=true); Lazer→off: essenciais 3.649,50 (restante inteiro); restauração OK. Falha de rede simulada (fetch rejeitado): estado anterior mantido, toggles reabilitados e mensagem clara 'Não foi possível salvar. Tente novamente.' (corrigi o fallback que exibia 'Failed to fetch' cru — agora ErroApi.mensagem para erros da API, fallback amigável para o resto; lógica duplicada nos dois handlers extraída para mensagemDeErroAoAlternar). AC #2: mes com alternarInvestir=null e alternarLazer='estranho' renderizou sem crash (Boolean defensivo: false/true). Console sem erros; tsc/build/lint OK. Sem mudanças no backend; estado do mês restaurado após os testes.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Bloco 'Configurações' no aside da tela do mês com switches de Investir e Lazer (sem essenciais — AC #5), estado persistido vindo do mes; useAlterarAlternancias (PATCH /meses/{id}/alternancias) invalida sumario+mes no sucesso — distribuição atualiza sem reload; sem update otimista: falha mantém estado e mostra mensagem inline; renderização defensiva de payload inesperado. Verificado em navegador real (4 combinações de toggles com valores corretos da distribuição, falha de rede simulada, payload null/string), tsc/build/lint OK, console limpo.
<!-- SECTION:FINAL_SUMMARY:END -->
