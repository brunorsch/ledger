---
id: TASK-20
title: Integrar Firebase Authentication e propagar usuario_id
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-19 00:35'
labels:
  - auth
  - backend
dependencies:
  - TASK-7
  - TASK-10
  - TASK-11
references:
  - backlog/tasks/task-21
modified_files:
  - server/build.gradle.kts
  - server/src/main/resources/application.yaml
  - >-
    server/src/main/resources/db/migration/V9__adicionar_sujeito_provedor_em_usuarios.sql
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/config/SegurancaConfig.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/config/SegurancaLocalConfig.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/config/FiltroAutenticacaoLocal.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/config/CorrelationIdFilter.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/service/UsuarioAutenticadoService.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/service/UsuarioAutenticadoLocal.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/controller/MesController.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/controller/LancamentoController.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/controller/GastoFixoController.kt
  - >-
    server/src/main/kotlin/br/dev/brunorsch/ledger/server/exception/ApiExceptionHandler.kt
  - server/src/test/kotlin/br/dev/brunorsch/ledger/server/SegurancaTeste.kt
  - server/src/test/kotlin/br/dev/brunorsch/ledger/server/SegurancaApiTest.kt
  - >-
    server/src/test/kotlin/br/dev/brunorsch/ledger/server/AutenticacaoLocalApiTest.kt
priority: medium
type: task
ordinal: 20000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Integrar o Firebase Authentication ao backend Spring Boot, propagando o `usuario_id` para todas as APIs (Ledger - Projeto.md, seção "Segurança e multiusuário"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- A decisão do projeto é começar com usuário único, mas todas as APIs já filtram por `usuario_id` desde o primeiro dia (tasks anteriores).
- O login entra antes do primeiro deploy (decisão sugerida em "Sugestoes de melhoria"), porque os dados são financeiros em servidor público.
- Provedores de login habilitados no Firebase: Google, Facebook e e-mail/senha.
- Fluxo: o front autentica com o SDK do Firebase e envia o ID token (JWT) em `Authorization: Bearer`. O backend não conduz o login nem mantém sessão: valida o token como resource server (`oauth2ResourceServer().jwt()`), com issuer `https://securetoken.google.com/<project-id>` e audience igual ao project id.
- O `sub` do token (uid do Firebase) é gravado em `sujeito_provedor`. A coluna `identity_provider` da tabela `usuarios` já existe e recebe o claim `firebase.sign_in_provider` (`google.com`, `facebook.com` ou `password`) do primeiro login.
- Facebook pode não devolver e-mail: o backend não pode assumir que `email` está presente no token.
- Um teste automatizado garante que um usuário não lê nem altera dados de outro.

Fora do escopo:
- Telas e fluxo de login no front (React): task separada.
- Refresh tokens persistidos em banco: o SDK do Firebase cuida disso no cliente.
- Login via Apple (exigiria conta paga do Apple Developer).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Projeto Firebase criado com os provedores Google, Facebook (app na Meta com App ID/secret e redirect URI do Firebase) e e-mail/senha habilitados
- [x] #2 Spring Security configurado como resource server JWT, validando assinatura, issuer e audience do ID token do Firebase, com project id configurável por env var
- [x] #3 Usuário novo é criado na tabela `usuarios` no primeiro request autenticado, gravando o uid em `sujeito_provedor` e o `firebase.sign_in_provider` em `identity_provider`
- [x] #4 Token de um uid já conhecido reaproveita o `usuario_id` existente, independentemente do provedor usado
- [x] #5 Todas as APIs passam a resolver `usuario_id` a partir do principal autenticado (helper da task de meses substituído)
- [x] #6 Requisição sem token, com token expirado ou de outro projeto Firebase retorna 401
- [x] #7 Teste integrado garante que um usuário não lê nem altera dados de outro (autorização por usuario_id)
- [x] #8 Rota `/actuator/health` permanece pública para probes externos
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Migration V9: coluna sujeito_provedor varchar(255) única em usuarios; remover constraint única de identity_provider.
2. SegurancaConfig: resource server JWT (issuer https://securetoken.google.com/${FIREBASE_PROJECT_ID}, audience = project id); /api/** autenticado; /actuator/health, /error e assets do SPA públicos.
3. UsuarioAutenticadoService passa a resolver pelo principal JWT (sub -> sujeito_provedor, upsert com sign_in_provider -> identity_provider); controllers trocam X-Usuario-Id por @AuthenticationPrincipal; CorrelationIdFilter lê usuarioId do SecurityContext.
4. Perfil local mantém fallback X-Usuario-Id (decisão do usuário); demais perfis exigem JWT.
5. Testes: SegurancaApiTest com decoder de teste (segredo HMAC) cobrindo 401 (sem token/expirado/outro projeto), criação/reuso de usuário e isolamento entre usuários; testes existentes passam a usar tokens de teste.
6. AC#9 (logout com Admin SDK) adiado por decisão do usuário (task futura).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test (21 suítes, 0 falhas) e :server:bootJar BUILD SUCCESSFUL. Starter usado: spring-boot-starter-security-oauth2-resource-server (o antigo está deprecated no 4.1). AC#6 coberto por SegurancaApiTest (sem token/expirado/audience errada/assinatura errada -> 401); AC#3/#4 por testes de upsert e preservação do provedor; AC#7 por isolamento entre uids; AC#8 health público. Testes migrados para tokens HMAC locais (decoder de teste com JwtIssuerValidator + JwtAudienceValidator). Perfil local mantém fallback X-Usuario-Id (AutenticacaoLocalApiTest). Claim firebase.sign_in_provider lido do objeto aninhado firebase.sign_in_provider com fallback plano (compatível com token real do Firebase).

AC#9 (logout com Admin SDK) movido para TASK-21 por decisão do usuário; AC#1 aguarda confirmação do usuário sobre provedores habilitados no projeto Firebase existente.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
TASK-20: backend valida ID tokens do Firebase como resource server JWT (issuer https://securetoken.google.com/${FIREBASE_PROJECT_ID}, audience = project id). Usuário criado no primeiro request (sub -> sujeito_provedor; sign_in_provider -> identity_provider, preservado nos logins seguintes); todos os controllers resolvem usuario_id pelo principal autenticado. /api/** exige token (401 sem/inválido); health e assets do SPA públicos. Perfil local mantém fallback X-Usuario-Id para o dev sem front de login. Verificado com suíte completa verde (inclui SegurancaApiTest e AutenticacaoLocalApiTest).
<!-- SECTION:FINAL_SUMMARY:END -->
