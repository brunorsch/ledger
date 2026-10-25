---
id: TASK-22
title: Compose de dev com postgres próprio e rede externa proxy
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-25 03:50'
updated_date: '2026-10-25 03:58'
labels: []
dependencies: []
priority: medium
ordinal: 22000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Deixa o compose.docker.yml autossuficiente, com o próprio postgres, e coloca a aplicação na rede externa proxy para o nginx alcançar o app.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 compose.docker.yml sobe app e postgres, sem depender do compose.yml
- [x] #2 App entra na rede externa proxy (criada na máquina) e na rede default, para resolver o postgres
- [x] #3 Banco do app configurado por DB_URL, DB_USER e DB_PASSWORD, sem perfil ou credencial fixos na imagem
- [x] #4 Documentação dos comandos e da rede proxy no AGENTS.md
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
4. Verificar o YAML e regerar o tar da imagem de dev conferindo o ambiente.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
compose.docker.yml agora tem os serviços postgres e app, com volume ledger-db-docker e healthcheck do postgres; o app depende de postgres:service_healthy e entra nas redes default e proxy (external: true, name: proxy).

O banco do app passou a vir de DB_URL/DB_USER/DB_PASSWORD no environment do compose. Por isso o profile docker e o application-docker.yml foram removidos: com o profile ativo, o spring.datasource.url do profile sobrescreveria o placeholder ${DB_URL} da application.yaml.

A rede proxy é externa e precisa existir na máquina (docker network create proxy). O nginx alcança a aplicação em http://app:9100 pelo nome do serviço.

Verificação: YAML validado (serviços postgres/app, redes default/proxy external, volume próprio) e regerado o tar da imagem de dev (server/build/jib-dev.tar): sem SPRING_PROFILES_ACTIVE, porta 9100, frontend em /app/resources/static e classes em /app/classes. Sem Docker na máquina, não foi possível subir o compose.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
O compose.docker.yml passa a ser autossuficiente: traz o próprio postgres (com volume e healthcheck dedicados) e sobe o app na rede externa proxy, onde o nginx enxerga a aplicação por http://app:9100. A imagem de dev deixou de fixar o profile docker e o application-docker.yml foi removido: o banco vem de DB_URL, DB_USER e DB_PASSWORD do ambiente, sobrescrevível por env.
<!-- SECTION:FINAL_SUMMARY:END -->
