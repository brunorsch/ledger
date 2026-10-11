---
id: TASK-18
title: Empacotar frontend e backend em um único JAR
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:44'
labels:
  - infra
dependencies:
  - TASK-1
  - TASK-2
  - TASK-13
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
- [ ] #1 `./gradlew bootJar` gera um JAR único contendo o backend e os assets do frontend em `dist/`
- [ ] #2 Executar o JAR com `java -jar` sobe o backend e serve `index.html` em rotas não-API
- [ ] #3 Assets com hash no nome respondem com `Cache-Control` longo (imutável)
- [ ] #4 `index.html` responde com `Cache-Control` que impede cache de versão antiga
- [ ] #5 Build local falha se o `vite build` falhar
- [ ] #6 Teste integrado verifica que uma rota SPA como `/meses/2026-11` devolve `index.html` e não 404
<!-- AC:END -->
