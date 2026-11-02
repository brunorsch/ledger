---
id: TASK-18
title: Empacotar frontend e backend em um único JAR
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-18 19:44'
labels:
  - infra
dependencies:
  - TASK-1
  - TASK-2
  - TASK-13
modified_files:
  - server/src/main/kotlin/br/dev/brunorsch/ledger/server/config/SpaWebConfig.kt
  - >-
    server/src/test/kotlin/br/dev/brunorsch/ledger/server/EmpacotamentoWebTest.kt
  - server/src/test/resources/static-fixture/index.html
  - server/src/test/resources/static-fixture/assets/app.a1b2c3.js
priority: medium
type: task
ordinal: 18000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Configurar o build para gerar um único JAR que contém o backend Spring Boot e o frontend React pronto para produção (Ledger - Projeto.md, seção "Stack e entrega" e "Empacotamento"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- O build do Gradle gera o build do Vite e o inclui no JAR.
- O backend serve o frontend: rotas da API (`/api/v1/*`) respondem JSON; demais rotas (SPA) devolvem `index.html`. A SPA roteia `/meses/{referencia}` no cliente.
- Arquivos com hash no nome ficam em cache longo; `index.html` fica sem cache.
- O PWA exige HTTPS (cuidado operacional, fora do escopo da task).

Fora do escopo:
- Geração de PWA (manifest, service worker) — a base PWA já é parte do setup, mas service worker e offline-first ficam para uma task dedicada posterior.
- Pipeline de deploy.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 `./gradlew bootJar` gera um JAR único contendo o backend e os assets do frontend em `dist/`
- [x] #2 Executar o JAR com `java -jar` sobe o backend e serve `index.html` em rotas não-API
- [x] #3 Assets com hash no nome respondem com `Cache-Control` longo (imutável)
- [x] #4 `index.html` responde com `Cache-Control` que impede cache de versão antiga
- [x] #5 Build local falha se o `vite build` falhar
- [x] #6 Teste integrado verifica que uma rota SPA como `/meses/2026-11` devolve `index.html` e não 404
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Validar bootJar existente (embala dist, depende de pnpmBuild) com ./gradlew :server:bootJar e jar tf; ajustar apenas se necessário.
2. Criar config/SpaWebConfig.kt: cache longo immutable para /assets/**, no-store para /index.html, fallback SPA para rotas sem handler (fora de /api e /actuator) devolvendo index.html.
3. Criar teste integrado EmpacotamentoWebTest.kt com fixture estática: rota SPA /meses/2026-11 -> 200 HTML; headers de cache; /api/v1/nao-existe -> 404 JSON.
4. Rodar ./gradlew :server:test e validar ACs.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test verde (135 testes). EmpacotamentoWebTest cobre AC#2/#3/#4/#6. bootJar BUILD SUCCESSFUL e o JAR contém BOOT-INF/classes/static/{index.html, assets/index-*.js, assets/index-*.css, favicon.svg}. Smoke test com java -jar real: /meses/2026-11 -> 200 HTML, /index.html -> no-store, /assets/index-*.js -> max-age=31536000 public immutable, /api/v1/nao-existe -> 404 JSON. AC#5 é garantido pelo dependsOn(':web:pnpmBuild') do bootJar (falha do vite build falha o build).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
TASK-18: JAR único com backend + assets do Vite. SpaWebConfig (config nova) serve /assets/** com cache imutável de 1 ano, /index.html e rotas SPA sem extensão (fora de /api e /actuator) com no-store e fallback para index.html; API/actuator inexistentes seguem 404 JSON. bootJar já embalava o dist (sem mudança de build). Verificado com EmpacotamentoWebTest (4 testes), bootJar + inspeção do JAR e smoke test java -jar contra Postgres efêmero (rotas SPA, cache headers e 404 JSON).
<!-- SECTION:FINAL_SUMMARY:END -->
