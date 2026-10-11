---
id: TASK-7
title: API para criar e obter mês com captura de parâmetros
status: To Do
assignee: []
created_date: '2026-10-11 17:39'
updated_date: '2026-10-11 17:45'
labels:
  - api
  - backend
dependencies:
  - TASK-1
  - TASK-3
  - TASK-6
priority: high
type: task
ordinal: 7000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Expor endpoints REST para criar e consultar meses do Ledger, gravando e respeitando a captura de parâmetros por mês (Ledger - Projeto.md, seções "Como o passado não muda" e "Dados"; nomenclatura em backlog/decisions/decision-1).

Contexto:
- Cada mês guarda seus próprios lançamentos concretos e os parâmetros (percentuais, tetos) ficam gravados no mês na criação.
- Alterar parâmetros hoje só vale pros meses novos.
- O backend resolve o `usuario_id` a partir do header de autenticação (helper criado nesta task); o usuário único do MVP pode vir de um header fixo ou de um usuário seed — a integração com OAuth entra em task dedicada.
- Dinheiro é serializado como string no JSON (ex.: `"1193.08"`); Jackson configurado com `USE_BIG_DECIMAL_FOR_FLOATS` para mapas genéricos.

Endpoints:
- `POST /api/v1/meses` — cria mês com mes_referencia, captura de parâmetros (percentuais e tetos), alternâncias de investir e lazer. Retorna 201 com o mês criado.
- `GET /api/v1/meses/{id}` — obtém mês por id (escopo do usuario_id).
- `GET /api/v1/meses?referencia=YYYY-MM` — obtém mês por referência (escopo do usuario_id).
- Validações: mes_referencia no primeiro dia do mês; captura com `versao_esquema`; alternâncias default = true.

Fora do escopo:
- Geração de lançamentos a partir de gastos fixos (task dedicada).
- Cálculo de sumário e distribuição (exposto em outra task).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 `POST /api/v1/meses` cria um mês e retorna 201 com o payload completo
- [ ] #2 Captura gravada no banco é igual à enviada na request (verificável por leitura posterior)
- [ ] #3 Mês com mes_referencia fora do primeiro dia do mês retorna 400 com mensagem clara
- [ ] #4 Mês sem `versao_esquema` na captura retorna 400
- [ ] #5 `GET /api/v1/meses/{id}` retorna 200 para mês do próprio usuário e 404 para mês inexistente ou de outro usuário
- [ ] #6 `GET /api/v1/meses?referencia=YYYY-MM` retorna o mês correspondente ou 404
- [ ] #7 Teste integrado com Testcontainers cobre o caminho feliz de cada endpoint
- [ ] #8 Teste integrado garante que um usuário não consegue ler mês de outro usuário (autorização por usuario_id)
- [ ] #9 Dinheiro serializado como string no JSON de resposta e request
<!-- AC:END -->
