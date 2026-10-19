---
id: TASK-21
title: Adicionar logout no backend com Firebase Admin SDK
status: To Do
assignee: []
created_date: '2026-10-19 00:34'
labels:
  - auth
  - backend
dependencies:
  - TASK-20
references:
  - backlog/tasks/task-20
ordinal: 21000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Completa a integração Firebase Authentication (TASK-20): o logout é feito no cliente (signOut do SDK), mas o backend deve expor endpoint autenticado que revoga os refresh tokens do usuário via Firebase Admin SDK. Requer credenciais de service account configuráveis por env (nunca commitadas).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 POST /api/v1/logout autenticado chama revokeRefreshTokens(uid) do Admin SDK e responde 204
- [ ] #2 Requisição sem token no endpoint retorna 401
- [ ] #3 Credenciais do service account vêm de env var (ex.: FIREBASE_SERVICE_ACCOUNT ou GOOGLE_APPLICATION_CREDENTIALS), sem segredo commitado
- [ ] #4 Teste automatizado cobre o endpoint com o Admin SDK mockado (sem depender de Firebase real)
<!-- AC:END -->
