---
id: TASK-2
title: Inicializar projeto frontend React com Vite e TypeScript
status: Done
assignee: []
created_date: '2026-10-11 17:37'
updated_date: '2026-10-12 19:33'
labels:
  - setup
  - frontend
dependencies: []
priority: high
type: task
ordinal: 2000
---
## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Configurar a base do cliente web do Ledger conforme os requisitos técnicos do projeto (Ledger - Projeto.md, seção "Requisitos técnicos / Stack e entrega").

Contexto:
- Stack: React (Vite/TS).
- A release é web com PWA instalável no celular; a versão dedicada mobile fica para depois.
- O artefato final é um único JAR que também serve o frontend; esse ponto é coberto em task dedicada de packaging.
- A formatação monetária em BRL fica centralizada no frontend, que nunca faz cálculo monetário — apenas exibe.

Escopo desta task (apenas o esqueleto do frontend):
- Projeto Vite com template React + TS.
- ESLint + TypeScript strict.
- Configuração de build de produção gerando assets em `dist/` com hashes no nome.
- Cliente HTTP mínimo com tipagem para a API (camada fina; detalhamento dos endpoints fica nas tasks verticais).
- Página única de smoke (`/`) que renderiza um texto estático, para validar a integração com o backend em tasks futuras.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 `npm run build` (ou equivalente) gera `dist/` sem erros e sem warnings de TS
- [ ] #2 `npm run lint` passa com a config estrita do projeto
- [ ] #3 Servido pelo `vite dev` durante o desenvolvimento e empacotado para produção em `dist/`
- [ ] #4 Tipagem de cliente HTTP é estendida com `tsc --noEmit` sem erros quando uma rota placeholder é consumida
- [ ] #5 Página de smoke renderiza texto estático e é o único ponto montado na raiz
<!-- AC:END -->
