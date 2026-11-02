---
id: TASK-30
title: Garantir criação de mês única e atômica
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - bug
  - backend
dependencies: []
references:
  - server/src/main/kotlin/br/dev/brunorsch/ledger/server/service/MesService.kt
priority: medium
type: bug
ordinal: 30000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
`MesService.criar` verifica se o mês já existe e só depois insere, e a tabela `meses` não tem restrição única em (usuario_id, mes_referencia). Duas requisições simultâneas (duplo clique, retry de rede) podem criar o mesmo mês duas vezes, e a partir daí a busca por referência fica ambígua. A criação também não roda em transação. Se a geração dos lançamentos a partir dos gastos fixos falhar, o mês fica gravado sem eles, e a nova tentativa responde que o mês já existe. Se o banco de produção já tiver meses duplicados, a restrição única não será aplicável sem tratá-los antes.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 O banco impede dois meses com a mesma referência para o mesmo usuário
- [ ] #2 Criar um mês que já existe retorna 409, inclusive quando duas requisições chegam ao mesmo tempo
- [ ] #3 Se a geração dos lançamentos a partir dos gastos fixos falhar, nem o mês nem nenhum lançamento é gravado
- [ ] #4 Testes de integração com Testcontainers cobrem a duplicata e o rollback
<!-- AC:END -->
