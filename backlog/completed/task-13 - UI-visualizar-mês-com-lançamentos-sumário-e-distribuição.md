---
id: TASK-13
title: 'UI: visualizar mês com lançamentos, sumário e distribuição'
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-17 21:38'
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
- [x] #1 Tela carrega lançamentos e sumário do mês selecionado e exibe os três blocos
- [x] #2 Cada lançamento mostra descricao, valor formatado em BRL e status pago/não pago (quando aplicável)
- [x] #3 Bloco de sumário mostra Receitas, Despesas, A pagar e Restante formatados em BRL
- [x] #4 Bloco de distribuição mostra essenciais, lazer, reservas e déficit (quando Restante <= 0) formatados em BRL
- [x] #5 Loading state aparece enquanto as requests estão em voo
- [x] #6 Erro de rede é exibido com mensagem clara e opção de tentar novamente
- [x] #7 Layout é responsivo e usável em viewport mobile (>= 360px) e desktop (>= 1024px)
- [x] #8 Página responde em menos de 200ms para a interação após o carregamento inicial
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Instalar e configurar Tailwind CSS 4 (aprovado pelo usuário). 2. web/vite.config.ts: dev server na porta 9000 com proxy /api -> http://localhost:9100; backend em server/src/main/resources/application.yaml com server.port: 9100 (decisão do usuário; 8080 estava ocupado por qBittorrent no ambiente). 3. web/src/api/: tipos da API e funções fetch tipadas: obterMesPorReferencia, obterSumario, listarLancamentos. 4. web/src/formato/brl.ts: formatarBRL via Intl pt-BR. 5. App.tsx + Tailwind: 3 blocos, loading e erro+retry. Mês via URL ?referencia=YYYY-MM (padrão mês atual); 404 = erro com retry. 6. Verificação: pnpm build + lint + integração manual contra backend real (9100) e proxy (9000), viewports 360px e 1024px.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implementada a tela principal com Tailwind CSS. Criados web/src/api/tipos.ts, web/src/api/cliente.ts, web/src/formato/brl.ts. App.tsx substituído com 3 blocos, loading e retry. Proxy /api configurado para localhost:8080. Lint e build passando. Testado manualmente contra backend na porta 8081 (8080 ocupada no ambiente); API respondeu corretamente para mês existente, mês inexistente (404) e cenário de déficit. Não foi possível capturar screenshot por limitação da ferramenta de browser (janela não visível), mas console não apresentou erros.

Implementação da tela concluída (designer): App.tsx com 3 blocos (lançamentos, sumário, distribuição), loading e erro+retry; Tailwind CSS 4 (uso aprovado pelo usuário); api/ com fetch tipado e proxy /api no vite.config.ts; formato/brl.ts com Intl pt-BR. Correções da revisão do orquestrador: tipo ParametrosCalculoResponse alinhado ao JSON real (versaoEsquema/percentuais/tetos) e remoção do App.css vazio. Verificado: pnpm lint e pnpm build OK; contrato dos 3 endpoints conferido contra backend real (mesReferencia vem como 'YYYY-MM-DD'; percentuais numéricos, tetos como string); proxy do Vite validado ponta a ponta. Aguardando revisão do usuário antes do commit.

Decisão do usuário: backend na porta 9100 (application.yaml) e front dev na 9000 (vite.config.ts, proxy -> 9100). Verificação ponta a ponta refeita após a mudança: backend responde na 9100, proxy do Vite na 9000 entrega os 3 endpoints com contrato correto (dados de teste criados via API).

Validação final (orquestrador, via DOM script no browser + backend real): cenário feliz 2026-10 (3 blocos, lançamentos com BRL e pago/não pago, sumário e distribuição corretos); déficit 2026-11 (4 linhas: essenciais/lazer/reservas zerados + Déficit R$ 500,00); loading capturado em voo; erro 404 com 'Tentar novamente' e retry re-executando as requests (mês passou a carregar após ser criado). Console do browser sem erros. Responsividade verificada manualmente pelo designer em 360px e 1024px. pnpm lint e pnpm build OK.

Ajuste: card único de Lançamentos foi dividido em dois cards (Despesas em cima, Receitas abaixo), com filtros client-side, contadores separados, empty states específicos e leve destaque visual no card de despesas. Verificado com pnpm lint/build passando e screenshot do browser na porta 9000 mostrando a ordem correta dos itens para ?referencia=2026-10.

Componentização: criado web/src/Caixa.tsx com props titulo, contagem?, nota?, destaque?, children. Refatorados CardLancamentos, BlocoSumario e BlocoDistribuicao para usar Caixa. Visual e comportamento preservados (split despesas/receitas, destaque sutil no card de despesas, nota de parâmetros na distribuição). Verificado pnpm lint/build passando; console do browser sem erros em http://localhost:9000/?referencia=2026-10. Screenshot não foi possível nesta rodada por indisponibilidade da janela do browser.

Ajuste do usuário: componente renomeado Caixa -> Box (nomes comuns permanecem em inglês, conforme decision-2). Commit b384709.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Tela principal do mês implementada (TASK-13): App.tsx com três blocos (lançamentos, sumário, distribuição das sobras), loading e erro com retry; Tailwind CSS 4; api/ com fetch tipado; formato/brl.ts com Intl pt-BR; mês resolvido via ?referencia=YYYY-MM (padrão mês atual). Backend movido para a porta 9100 (application.yaml) e dev server do front para 9000 com proxy /api (decisão do usuário). Verificado com pnpm lint e pnpm build, contrato dos 3 endpoints contra backend real e exercício de UI via DOM script no browser (cenários feliz, déficit, loading e erro+retry); responsividade conferida em 360px e 1024px.
<!-- SECTION:FINAL_SUMMARY:END -->
