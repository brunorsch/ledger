---
id: TASK-7
title: API para criar e obter mês com captura de parâmetros
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:39'
updated_date: '2026-10-16 22:42'
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
- [x] #1 `POST /api/v1/meses` cria um mês e retorna 201 com o payload completo
- [x] #2 Captura gravada no banco é igual à enviada na request (verificável por leitura posterior)
- [x] #3 Mês com mes_referencia fora do primeiro dia do mês retorna 400 com mensagem clara
- [x] #4 Mês sem `versao_esquema` na captura retorna 400
- [x] #5 `GET /api/v1/meses/{id}` retorna 200 para mês do próprio usuário e 404 para mês inexistente ou de outro usuário
- [x] #6 `GET /api/v1/meses?referencia=YYYY-MM` retorna o mês correspondente ou 404
- [x] #7 Teste integrado com Testcontainers cobre o caminho feliz de cada endpoint
- [x] #8 Teste integrado garante que um usuário não consegue ler mês de outro usuário (autorização por usuario_id)
- [x] #9 Dinheiro serializado como string no JSON de resposta e request
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Migration V6 renomeia captura_parametros para parametros_calculo e V7 alinha o CHECK à chave camelCase versaoEsquema do JSON armazenado. 2. decision-3 registra a renomeação para parametrosCalculo (decision-1 superada nesse ponto; o id decision-2 foi usado pelo usuário). 3. Domínio: ParametrosCalculo (versaoEsquema + percentuais + tetos) e entidade Mes (Spring Data JDBC) com validação de mes_referencia no primeiro dia. 4. Persistência: MesRepository + converters JSONB (PGobject) e Dinheiro<->BigDecimal. 5. Serviços: MesService (criar/obter por id/obter por referência) e UsuarioAutenticadoService (header X-Usuario-Id com fallback seed id 1). 6. API: POST /api/v1/meses (201), GET /api/v1/meses/{id} e GET /api/v1/meses?referencia=YYYY-MM; exceções 400/404/401 com mensagens claras; Dinheiro como string no JSON (serializer/deserializer Jackson). 7. Testes: unitário da validação de mes_referencia; MesApiTest integrado (MockMvc + Testcontainers) cobrindo os ACs #1-#9.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test -> BUILD SUCCESSFUL, 66 testes (0 falhas) em PostgreSQL real via Testcontainers. Smoke manual com bootRun local (porta 8090): POST 201 com payload completo, GET por referência 200, 404 para mês ausente, 400 com mensagem clara para referência fora do primeiro dia, 404 para mês de outro usuário e 401 para usuário inexistente no header. Descobertas: no Spring Boot 4.1/Jackson 3, USE_BIG_DECIMAL_FOR_FLOATS vive em spring.jackson.deserialization.* e BigDecimal já é escrito com toPlainString por padrão; o CHECK da V2 exigia a chave snake_case versao_esquema, incompatível com o JSON camelCase aprovado, então a V7 alinha o CHECK a versaoEsquema. O CLI do backlog não aceita corpo de decisão fora de editor interativo, então o corpo da decision-3 fica para o usuário preencher.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
API REST de meses com parametrosCalculo: POST /api/v1/meses (201, valida mes_referencia no primeiro dia, versaoEsquema obrigatório, alternâncias default true), GET /api/v1/meses/{id} e GET /api/v1/meses?referencia=YYYY-MM com escopo por usuario_id (header X-Usuario-Id com fallback seed). Dinheiro serializado como string; captura JSONB via converters PGobject; migrations V6 (renomeia coluna) e V7 (alinha CHECK ao camelCase); decision-3 registra a renomeação. Verificado com ./gradlew :server:test (66 testes, 0 falhas, Testcontainers postgres:17) e smoke manual via curl no bootRun local (201/200/400/401/404 conforme cada cenário).
<!-- SECTION:FINAL_SUMMARY:END -->
