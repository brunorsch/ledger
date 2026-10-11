---
id: TASK-20
title: Integrar login OAuth e propagar usuario_id
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:45'
labels:
  - auth
  - backend
dependencies:
  - TASK-7
  - TASK-10
  - TASK-11
priority: medium
type: task
ordinal: 20000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Integrar login OAuth no backend Spring Boot, propagando o `usuario_id` para todas as APIs (Ledger - Projeto.md, seção "Segurança e multiusuário"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- A decisão do projeto é começar com usuário único, mas todas as APIs já filtram por `usuario_id` desde o primeiro dia (tasks anteriores).
- O login entra antes do primeiro deploy (decisão sugerida em "Sugestoes de melhoria"), porque os dados são financeiros em servidor público.
- Para login social, usar OIDC (camada de autenticação sobre OAuth2). No Spring Security isso é o `oauth2Login`.
- A coluna `sujeito_provedor` da tabela `usuarios` já existe e fica populada por esta task.
- Um teste automatizado garante que um usuário não lê nem altera dados de outro.

Fora do escopo:
- Refresh tokens persistidos em banco (fica para iteração posterior se necessário).
- Configuração específica de cada IdP (a task usa um provider configurável por variáveis de ambiente).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Spring Security habilita `oauth2Login` com um provedor OIDC configurável por env vars
- [ ] #2 Usuário novo é criado na tabela `usuarios` no primeiro login, gravando `sujeito_provedor` retornado pelo IdP
- [ ] #3 Login existente reaproveita o `usuario_id` do mesmo `sujeito_provedor`
- [ ] #4 Todas as APIs passam a resolver `usuario_id` a partir do principal autenticado (helper da task de meses substituído)
- [ ] #5 Teste integrado garante que um usuário não lê nem altera dados de outro (autorização por usuario_id)
- [ ] #6 Rota `/actuator/health` permanece pública para probes externos
- [ ] #7 Logout encerra a sessão local e revoga o token no IdP quando aplicável
<!-- AC:END -->
