---
id: TASK-3
title: Implementar tipo Dinheiro com BigDecimal e arredondamento HALF_UP
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:37'
updated_date: '2026-10-12 20:50'
labels:
  - domain
  - backend
dependencies: []
priority: high
type: task
ordinal: 3000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar o value class `Dinheiro` que representa todo valor monetário no Ledger (Ledger - Projeto.md, seção "Dinheiro"; decisão de nomenclatura em backlog/decisions/decision-1).

Contexto:
- Valores monetários são `BigDecimal` no Kotlin e `NUMERIC(14,2)` no PostgreSQL, sempre com escala 2.
- O domínio tem um value class em volta do `BigDecimal`, que normaliza a escala na criação, porque `BigDecimal.equals` considera a escala (`2.0` é diferente de `2.00`).
- Existe um único modo de arredondamento, explícito, aplicado no domínio; decisão confirmada com o usuário: HALF_UP.
- Os testes comparam valores monetários por `compareTo`, não por `equals`.
- A API envia e recebe valores monetários como string (ex.: `"1193.08"`), para o JS não converter para `double`.

Fora do escopo:
- Serialização Jackson (tratada na task da API).
- Formatação BRL no frontend (tratada em task de UI).

Notas técnicas:
- A normalização acontece no construtor/factory, aplicando `setScale(2, RoundingMode.HALF_UP)` quando o valor recebido tem outra escala.
- Operações aritméticas (`plus`, `minus`, `times`, `divideBy` com escala explícita) também usam HALF_UP.
- O tipo aceita apenas valores `>= 0` no construtor padrão (validação por invariante); existe uma factory separada para casos em que o domínio precise de sinal (ex.: déficit).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 `Dinheiro("1193.08")` e `Dinheiro(BigDecimal("1193.080"))` representam o mesmo valor e `compareTo == 0`
- [x] #2 Dois `Dinheiro` com mesma grandeza e escalas diferentes (ex.: `2.0` vs `2.00`) têm `compareTo == 0` mas `equals == false` (comportamento documentado)
- [x] #3 `Dinheiro("0.125").somar(Dinheiro("0"))` resulta em `Dinheiro("0.13")` após HALF_UP
- [x] #4 Construtor padrão rejeita valor negativo com exceção explícita
- [x] #5 Factory explícita para valor negativo (`Dinheiro.allowNegative`) é usada na representação do déficit do mês
- [x] #6 `Dinheiro.ZERO` é um singleton igual a qualquer `Dinheiro("0")` por `compareTo`
- [x] #7 Cobertura de testes unitários alcança 100% das linhas do value class
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Criar value class Dinheiro (server/src/main/kotlin/.../dominio/Dinheiro.kt) com construtor interno (Long centavos), companion factories para String/BigDecimal/Int, ZERO singleton, allowNegative.
2. Operações: somar, subtrair, multiplicar(BigDecimal), dividir(divisor, escala), abs, unaryMinus, compareTo, toString. HALF_UP em todos.
3. Testes unitários cobrindo AC 1-7 com 100% de cobertura.
4. Validar com ./gradlew :server:test --tests DinheiroTest.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implementado Dinheiro (value class sobre BigDecimal) em server/src/main/kotlin/br/dev/brunorsch/ledger/server/dominio/Dinheiro.kt: construtor privado + factories invoke (String/BigDecimal) que validam >= 0 e normalizam escala > 2 com HALF_UP; allowNegative para sinal (déficit); ZERO singleton; somar/subtrair/multiplicarPor/dividirPor(escala) normalizam com HALF_UP. equals segue semântica do BigDecimal (sensível à escala) — uso de compareTo documentado no KDoc.

Validação: SPRING_PROFILES_ACTIVE=local ./gradlew :server:test :server:jacocoTestReport → 14/14 testes passando (13 de DinheiroTest + contextLoads pré-existente). JaCoCo: Dinheiro 0/6 linhas perdidas, Dinheiro.Companion 0/10 → 100% de cobertura de linhas.

Decisão: subtrair lança IllegalArgumentException em resultado negativo (invariante não-negativo preservado; déficit via allowNegative). Adicionado plugin jacoco ao server/build.gradle.kts apenas para verificação do AC #7.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Criado o value class Dinheiro (server/src/main/kotlin/br/dev/brunorsch/ledger/server/dominio/Dinheiro.kt) com invariante não-negativo, normalização HALF_UP na criação e nas operações (somar, subtrair, multiplicarPor, dividirPor), factory allowNegative para déficit e ZERO singleton. Testes em DinheiroTest.kt cobrem os 7 ACs; suíte completa passa (14/14) e JaCoCo mostra 100% de cobertura de linhas da classe.
<!-- SECTION:FINAL_SUMMARY:END -->
