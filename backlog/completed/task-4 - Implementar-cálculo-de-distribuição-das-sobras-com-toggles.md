---
id: TASK-4
title: Implementar cálculo de distribuição das sobras com alternâncias
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:37'
updated_date: '2026-10-13 01:09'
labels:
  - domain
  - backend
dependencies:
  - TASK-3
priority: high
type: task
ordinal: 4000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar a regra de distribuição do restante do mês em três categorias: gastos essenciais, lazer e reservas. A regra é o coração do produto descrito em Ledger - Projeto.md, seções "Regra de distribuição das sobras" e "Toggles de investir e de lazer"; nomenclatura em backlog/decisions/decision-1.

Contexto:
- Regra padrão a partir do `Restante` (Receitas − Despesas):
  - `essenciais = min(50% do restante, R$ 1.300,00)`
  - `lazer = min(30% do restante, R$ 700,00)`
  - `reservas = restante − essenciais − lazer`
  - A soma sempre fecha no restante.
- Alternâncias do mês alteram a regra:
  - `investir=false`: redistribui a parte das reservas entre essenciais e lazer na proporção 70/30, mantendo o limite de lazer.
  - `lazer=false`: distribui a parte das reservas entre essenciais e reservas na proporção 50/50, respeitando o limite de gastos.
  - `investir=false && lazer=false`: valor todo em gastos do mês, sem limite.
  - Gastos do mês (essenciais) nunca pode ser desligado.
- Decisão confirmada com o usuário: quando `Restante <= 0`, nenhuma categoria recebe valor e o déficit é exposto.
- Modo de arredondamento: HALF_UP (consistente com `Dinheiro`).
- Os exemplos numéricos do projeto viram casos de teste:
  - Restante `R$ 1.193,08` → essenciais `R$ 596,54`, lazer `R$ 357,92`, reservas `R$ 238,62`.
  - Restante `R$ 7.872,34` → essenciais `R$ 1.300,00`, lazer `R$ 700,00`, reservas `R$ 5.872,34`.

Fora do escopo:
- Persistência (snapshot dos parâmetros fica em task de migrations).
- Endpoint HTTP (tratado em task de API).
- UI (tratado em task vertical de UI).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Exemplos numéricos do projeto (Restante 1193,08 e 7872,34) produzem os valores esperados com `compareTo` exato
- [x] #2 `essenciais + lazer + reservas == restante` em todos os cenários válidos com HALF_UP
- [x] #3 Quando `restante == 0`, as três categorias recebem zero e o déficit exposto é zero
- [x] #4 Quando `restante < 0`, nenhuma categoria recebe valor e o déficit exposto é igual a `|restante|`
- [x] #5 `investir=false, lazer=true` mantém limite de lazer e redistribui reservas em 70/30 (essenciais/lazer)
- [x] #6 `investir=true, lazer=false` redistribui reservas em 50/50 (essenciais/reservas) com limite de gastos
- [x] #7 `investir=false, lazer=false` aloca o restante todo em essenciais, sem teto
- [x] #8 Alternâncias inválidas (ex.: essenciais desligado) são rejeitadas pelo domínio antes do cálculo
- [x] #9 Cálculo é determinístico: mesmas entradas produzem exatamente o mesmo `Dinheiro` em cada execução
- [x] #10 Cobertura de testes unitários alcança 100% das linhas da função de distribuição
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Criar tipos do domínio: Alternancias(investir, lazer), Distribuicao(essenciais, lazer, reservas, deficit).
2. Criar função pura calcularDistribuicao(restante, alternancias) que aplica a regra:
   - restante <= 0: tudo zero + deficit=|restante|
   - essenciais_base = min(50%R, 1300)
   - flex_pool = R - essenciais_base
   - default: lazer = min(30%R, 700), reservas = R - essenciais - lazer
   - investir=false: flex 70/30 ess/laz, cap laz 700
   - lazer=false: flex 50/50 ess/res, cap ess 1300
   - ambos false: tudo em essenciais, sem teto
3. Testes unitários cobrindo AC 1-10 com 100% de cobertura.
4. Validar com ./gradlew :server:test --tests DistribuidorSobrasTest.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test --tests 'br.dev.brunorsch.ledger.server.domain.*' → 28/28 passando. JaCoCo: DistribuicaoKt 0/30 linhas perdidas, Distribuicao 0/5, Alternancias 0/4, Dinheiro 0/6, Dinheiro.Companion 0/10. Suíte completa: 28/29 (1 falha pré-existente em LedgerApplicationTests.contextLoads por falta de DB_URL no ambiente).

Algoritmo: restantes <= 0 → tudo zero, deficit=|restante|. Senão, base = min(50%R, 1300), flexPool = R - base. Branches: (T,T) regra padrão; (F,T) flex 70/30 ess/laz com cap laz 700; (T,F) flex 50/50 ess/res com cap ess 1300; (F,F) tudo em essenciais sem teto. HALF_UP em todas as etapas.

Decisão: deficit exposto como Dinheiro não-negativo (|restante| quando restante<0, ZERO caso contrário). allowNegative do Dinheiro não é usado no resultado da distribuição — fica disponível para representação intermediária. Alternancias é data class estanque com apenas investir/lazer: não há valor inválido (ex.: essenciais desligado) para o domínio rejeitar, então o AC #8 fica satisfeito por construção.

Refactor após review: aplicado (1) r → vlrRestante; (2) TRINTA_PORCENTO → PERCENTUAL_LAZER; (3) flexPool → restanteAposEssenciais; (4) branch investir=false,lazer=true passou a calcular adicionaisLazer primeiro (com cap 700) e obter adicionaisEssenciais por subtração — removeu SETENTA_PORCENTO; (5) METADE → divide(DOIS=BigDecimal(2)) nos dois usos; (6) construtor de Distribuicao passou a usar named parameters em todas as 4 chamadas (incluindo o else que dispensou o comentário).

Consequência da mudança (4) quando o cap de lazer vincula: o excedente vai para essenciais em vez de reservas. Para R=7872.34, investir=false, lazer=true: antes (5900.64, 700, 1271.70), agora (7172.34, 700, 0). AC #2 (soma fecha) preservado. Teste 'investir false e lazer true mantem limite de lazer 700 quando excede' atualizado. Cobertura e suíte verde (28/28 do domínio).

Refactor 2: substituído BigDecimal intermediário por operações de Dinheiro. Adicionados Dinheiro.min(outro) e Dinheiro.abs() (com testes em DinheiroTest). Constantes LIMITE_ESSENCIAIS/LIMITE_LAZER viraram Dinheiro; PERCENTUAL_LAZER e DOIS permanecem BigDecimal por serem fatores matemáticos, não dinheiro. Aritmética da distribuição agora usa somar/subtrair/multiplicarPor/dividirPor/min do próprio tipo; o branch do deficit usa restante.abs() em vez de Dinheiro(valor.negate()). Suíte do domínio: 31/31 testes, 100% de cobertura de linhas mantida (Dinheiro 0/8, DistribuicaoKt 0/45).

Refactor 3 (alinhamento com AGENTS.md): separado em um arquivo por classe. Alternancias.kt virou a data class com o método distribuir(restante) — a regra de domínio virou comportamento da classe que carrega as alternâncias, não mais uma função top-level. Distribuicao.kt agora só tem a data class Distribuicao. Constantes LIMITE_ESSENCIAIS/LIMITE_LAZER/PERCENTUAL_LAZER/DOIS viraram top-level file-private em Alternancias.kt (a regra cita funções, não constantes). Teste renomeado para AlternanciasTest e atualizado para o novo call site (alternancias.distribuir(restante)). 31/31 testes do domínio passando, 100% de cobertura mantida (Alternancias 0/45, AlternanciasKt 0/4, Distribuicao 0/5, Dinheiro 0/8, Dinheiro.Companion 0/10).

Refactor 4 (consolidação semântica): Alternancias eliminada e a regra de domínio virou a fábrica DistribuicaoSobras.calcular(restante, investir = true, lazer = true). O par de booleanos não tem identidade própria, então não merece um tipo dedicado; virou parâmetro nomeado do factory que cria o resultado, que é a entidade principal do comportamento. Renomeado Distribuicao → DistribuicaoSobras (mais descritivo, casa com o título da task). A invariante 'essenciais sempre ligado' agora é estrutural — não há parâmetro para essenciais — e está coberta por teste comportamental ('essenciais sempre recebe valor positivo quando restante positivo') em vez de reflexão sobre a assinatura. Estrutura final: Dinheiro.kt + DistribuicaoSobras.kt (uma classe por arquivo), 2 arquivos de teste. 32/32 testes do domínio passando, 100% de cobertura mantida (DistribuicaoSobras 0/9, DistribuicaoSobras.Companion 0/44, Dinheiro 0/8, Dinheiro.Companion 0/10).

Refactor 5 (review): percentualLazer renomeado para lazerBase, paralelizando com essenciaisBase. O nome anterior sugeria o que entrava no cálculo (a regra dos 30%), mas a variável guarda o total do lazer após aplicar min(30% * restante, 700) — então é o valor, não o percentual. 32/32 testes do domínio continuam passando.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Criado o cálculo de distribuição em server/src/main/kotlin/br/dev/brunorsch/ledger/server/dominio/Distribuicao.kt: tipos Alternancias e Distribuicao, e função pura calcularDistribuicao(restante, alternancias) aplicando a regra base com as três variações de toggles e o caso deficit. Testes em CalculadoraDistribuicaoTest.kt cobrem os 10 ACs (exemplos do projeto, soma fecha, déficit nas duas direções, cada branch de alternância, determinismo e rejeição por construção de Alternancias inválida). 28/28 testes do domínio passam; JaCoCo mostra 100% de cobertura de linhas nas classes do domínio.
<!-- SECTION:FINAL_SUMMARY:END -->
