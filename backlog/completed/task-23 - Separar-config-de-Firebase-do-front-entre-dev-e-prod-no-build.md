---
id: TASK-23
title: Separar config de Firebase do front entre dev e prod no build
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-27 05:48'
updated_date: '2026-10-27 06:03'
labels: []
dependencies: []
priority: high
ordinal: 23000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
O front embute a config do Firebase em tempo de build via web/.env (Vite). O .env local passou a ter valores do projeto dev (pregsledger-dev), então o jibProd passaria a embutir config de dev silenciosamente. Precisa haver um .env de prod (fora do git, por decisão do usuário) e o build precisa escolher o modo certo por imagem: jibProd embute prod, jibDev embute dev. De quebra, o FIREBASE_PROJECT_ID do compose.docker.yml está no serviço errado (postgres), o que faria o backend do docker-dev validar tokens contra o projeto prod.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 web/.env.production (fora do git) contém as configs do projeto prod e vence sobre .env no vite build (modo production)
- [x] #2 jibProd/jibProdTar embutem a config de prod sem flags extras
- [x] #3 jibDev/jibDevTar embutem a config de dev (modo development do Vite)
- [x] #4 compose.docker.yml passa FIREBASE_PROJECT_ID=pregsledger-dev no serviço app, não no postgres
- [x] #5 .gitignore cobre web/.env.production e os comandos/documentação (AGENTS.md e .env.example) refletem o fluxo dev/prod
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Criar web/.env.production com os valores de prod (pregsledger) e adicionar web/.env.production ao .gitignore.
2. web/build.gradle.kts: pnpmBuild passa a usar `pnpm run build -- --mode <viteModo>`, com viteModo vindo de -PviteModo (default production).
3. server/build.gradle.kts: jibDev/jibDevTar exigem -PviteModo=development (falham rápido com mensagem clara se esquecer); jibProd/jibProdTar seguem sem flags.
4. compose.docker.yml: mover FIREBASE_PROJECT_ID=pregsledger-dev do serviço postgres para o app.
5. Docs: nota no web/.env.example sobre .env.production; AGENTS.md atualiza comandos jibDev/jibDevTar com a flag.
6. Verificar: buildar jibProdTar e jibDevTar e inspecionar o bundle estático embutido (config de Firebase em cada um); rodar testes existentes para garantir que nada quebrou.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verificação: jibProdTar (sem flags) embute pregsledger.firebaseapp.com + apikey de prod no static; jibDevTar -PviteModo=development embute pregsledger-dev; guard whenReady falha com mensagem clara sem a flag. Causas corrigidas no caminho: pnpm 12 exige pnpm-workspace.yaml com allowBuilds false para @firebase/util e protobufjs (ERR_PNPM_IGNORED_BUILDS); o repasse de args do pnpm run inclui o '--' literal (comando errado 'vite build -- --mode'), então a task usa pnpm run build --mode direto; pnpmBuild ganhou inputs.property(viteModo) + inputs de .env/.env.production para não ser considerado up-to-date entre modos. Testes: ./gradlew :server:test BUILD SUCCESSFUL.

Ajuste pós-revisão: compose.prod.yml passa a ler DB_URL/DB_USER/DB_PASSWORD de env_file: .env (arquivo local não versionado) e publica na porta 8101. Validado com docker compose -f compose.prod.yml config. web/.env.prod removido (redundante com .env.production).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Separadas as configs de Firebase por ambiente: web/.env.production (ignorado pelo git) vence no modo production; jibDev/jibDevTar exigem -PviteModo=development e embutem a config de dev; compose.docker.yml valida o JWT contra pregsledger-dev (FIREBASE_PROJECT_ID no serviço app); pnpm-workspace.yaml documenta a decisão de não rodar build scripts de @firebase/util e protobufjs. Verificado inspecionando o bundle estático de cada imagem (tar dev e prod) e com a suíte de testes.
<!-- SECTION:FINAL_SUMMARY:END -->
