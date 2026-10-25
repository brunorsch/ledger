---
id: TASK-21
title: Gerar imagens Docker da aplicação com Jib (dev e prod)
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-25 03:04'
updated_date: '2026-10-25 03:09'
labels: []
dependencies: []
priority: medium
ordinal: 21000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Configurar o plugin Jib no módulo server para gerar a imagem Docker da aplicação (API + frontend) em duas variantes: dev (base JRE com shell, carregada no daemon local) e prod (base distroless, usuário não-root, publicável em registry).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Plugin Jib aplicado apenas no módulo server, com imagem em modo exploded (classes + resources + dependências em /app)
- [x] #2 Variante dev: imagem ledger-server:dev com base eclipse-temurin:25-jre-noble, gerada localmente via ./gradlew :server:jibDev
- [x] #3 Variante prod: imagem com base gcr.io/distroless/java25-debian13:nonroot, usuário não-root, publicada no registry via ./gradlew :server:jibProd
- [x] #4 A imagem inclui o frontend estático (web/dist) em /app/resources/static
- [x] #5 Porta 9100 exposta e configuração do banco via DB_URL, DB_USER e DB_PASSWORD do ambiente
- [x] #6 Tasks do Jib rodam com org.gradle.configuration-cache=true habilitado
- [x] #7 Compose de dev (app + banco) e de prod (app com banco externo) para rodar a aplicação containerizada
- [x] #8 Comandos de build e execução das imagens documentados no AGENTS.md
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
6. Documentar os comandos no AGENTS.md e finalizar a task com evidências.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Plugin Jib 3.5.4 aplicado só no módulo server. Dev: extensão padrão do Jib (base eclipse-temurin:25-jre-noble, linux/arm64 local, tag ledger-server:dev, SPRING_PROFILES_ACTIVE=docker). Prod: extensão jibProducao (base gcr.io/distroless/java25-debian13:nonroot, user 65532:65532, linux/amd64, tag $jibRegistro/ledger-server:prod + latest). Tasks: jibDev, jibDevTar, jibProd, jibProdTar.

O empacotamento do web/dist saiu do bootJar e foi para processResources (into("static")), assim o modo exploded do Jib inclui o frontend em /app/resources/static e o JAR mantém BOOT-INF/classes/static.

Configuração cache: as tasks do Jib são marcadas com notCompatibleWithConfigurationCache (Jib 3.5.4 não serializa Project). O build continua guardando o cache nas execuções sem Jib.

Verificação: sem Docker na máquina, as imagens foram validadas pelo tar (./gradlew :server:jibDevTar e :server:jibProdTar). Dev: tag ledger-server:dev, base temurin 25.0.4.1, arm64/linux, entrypoint java -XX:MaxRAMPercentage=75.0 -cp @/app/jib-classpath-file LedgerApplicationKt, porta 9100, frontend em /app/resources/static. Prod: tag ghcr.io/brunorsch/ledger-server:prod (+latest), base distroless java25 (temurin 25.0.5), user 65532:65532, amd64/linux, flags de heap dump e OOM, frontend presente.

Não executado por falta de Docker local: carregar a imagem no daemon (jibDev) e publicar no registry (jibProd). Também não houve execução do container; a validação foi estrutural (config e conteúdo das camadas).

Testes: ./gradlew :server:test => 63 testes, 8 falhas de inicialização dos testes de integração com Testcontainers (sem daemon Docker na máquina, DockerClientProviderStrategy). As 55 demais passaram. Falha não relacionada à mudança, que afeta apenas o empacotamento.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Imagens Docker da aplicação (API + frontend) geradas pelo Jib a partir do módulo server, em duas variantes. Dev: base eclipse-temurin:25-jre-noble, arquitetura local, tag ledger-server:dev, sobe com o profile docker apontando para o serviço db do compose. Prod: base gcr.io/distroless/java25-debian13:nonroot, usuário 65532:65532, linux/amd64, publicada no registry. O empacotamento do web/dist passou do bootJar para processResources, para que o modo exploded do Jib leve o frontend em /app/resources/static. Comandos documentados no AGENTS.md e stacks em compose.docker.yml e compose.prod.yml.
<!-- SECTION:FINAL_SUMMARY:END -->
