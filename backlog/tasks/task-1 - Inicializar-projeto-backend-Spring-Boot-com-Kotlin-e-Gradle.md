---
id: TASK-1
title: Inicializar projeto backend Spring Boot com Kotlin e Gradle
status: To Do
assignee: []
created_date: '2026-10-11 17:37'
updated_date: '2026-10-11 17:45'
labels:
  - setup
  - backend
dependencies: []
priority: high
type: task
ordinal: 1000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Configurar a base do serviço backend do Ledger conforme os requisitos técnicos do projeto (Ledger - Projeto.md, seção "Requisitos técnicos / Stack e entrega"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Stack: Spring Boot com Gradle, Kotlin, PostgreSQL.
- Toda tabela de dados do usuário precisa de `usuario_id` desde o primeiro dia, mesmo começando com usuário único.
- Migrações gerenciadas com Flyway desde o primeiro dia.
- Testes de integração precisam usar PostgreSQL real via Testcontainers (o comportamento de NUMERIC e JSONB difere de H2).
- O artefato final é um único JAR que também serve o frontend; esse ponto é coberto em task dedicada de packaging.

Escopo desta task (apenas o esqueleto do backend):
- Estrutura de módulos Gradle.
- Dependências iniciais: Spring Boot Web, Spring Data JPA, Flyway, PostgreSQL driver, Jackson Kotlin module, Testcontainers (PostgreSQL + JUnit), AssertJ.
- Configuração de profiles (dev, test) com application.yml.
- Pipeline de CI local: `./gradlew build` precisa passar sem código de produto, executando um teste de smoke vazio contra Testcontainers.
- Endpoint `/actuator/health` já respondendo 200; a ampliação da observabilidade fica para task dedicada.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Projeto Gradle compila com `./gradlew build` em ambiente limpo
- [ ] #2 Spring Boot sobe no perfil `dev` apontando para PostgreSQL via variáveis de ambiente
- [ ] #3 Flyway conecta no startup e cria o schema inicial vazio com `baseline_on_migrate` configurado
- [ ] #4 Testcontainers sobe um PostgreSQL real no perfil `test` e um teste de smoke passa
- [ ] #5 Build falha se uma migration do Flyway não puder ser aplicada no PostgreSQL do Testcontainers
- [ ] #6 Dependências do backend declaram Java/Kotlin compatíveis com a versão LTS escolhida na task
<!-- AC:END -->
