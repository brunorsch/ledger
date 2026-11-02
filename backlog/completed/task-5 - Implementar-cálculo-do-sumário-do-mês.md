---
id: TASK-5
title: Implementar cálculo do sumário do mês
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-11 17:37'
updated_date: '2026-10-13 02:45'
labels:
  - domain
  - backend
dependencies:
  - TASK-3
priority: high
type: task
ordinal: 5000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implementar o cálculo do sumário do mês descrito em Ledger - Projeto.md, seção "Definições do sumário do mês"; nomenclatura em backlog/decisions/decision-1.

Definições:
- `Receitas` = soma dos lançamentos de receita do mês.
- `Despesas` = soma dos lançamentos de despesa do mês.
- `A pagar` = soma das despesas ainda não marcadas como pagas.
- `Restante` = `Receitas − Despesas`.

Contexto:
- Tipos monetários em `Dinheiro` com HALF_UP.
- O sumário é sempre derivado dos lançamentos do próprio mês (nunca recalculado a partir de parâmetros atuais de outro mês).
- O sumário é puro: não toca em Spring, persistência ou qualquer infraestrutura.
- As alternâncias de investir/lazer não alteram o sumário (afetam apenas a distribuição das sobras).

Fora do escopo:
- Distribuição das sobras (coberta em task dedicada).
- Persistência do sumário (o sumário é derivado, não armazenado).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Mês sem lançamentos retorna `Receitas = Despesas = A pagar = Restante = 0`
- [x] #2 `Receitas` soma todos os lançamentos de receita do mês em `Dinheiro`
- [x] #3 `Despesas` soma todos os lançamentos de despesa (pagas e não pagas)
- [x] #4 `A pagar` soma apenas despesas com flag `pago = false`
- [x] #5 `Restante = Receitas − Despesas`, podendo ser negativo quando despesas superam receitas
- [x] #6 Alterar o status pago de uma despesa recalcula `A pagar` (teste de invariante explícito)
- [x] #7 Remover ou adicionar um lançamento recalcula o sumário (teste de invariante explícito)
- [x] #8 Sumário é determinístico: mesmos lançamentos produzem exatamente o mesmo resultado
- [x] #9 Cobertura de testes unitários alcança 100% das linhas do cálculo de sumário
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Adicionar Dinheiro.subtrairPermitindoNegativo(outro) (com teste) — Restante pode ser negativo e o subtrair atual rejeita.
2. Criar enum TipoLancamento {DESPESA, RECEITA} em arquivo próprio.
3. Criar data class Lancamento(valor, tipo, pago, descricao) em arquivo próprio.
4. Criar Sumario (data class + factory Sumario.calcular(lancamentos) no companion), em arquivo próprio. Cálculo em passada única somando receitas, despesas e aPagar; restante = receitas.subtrairPermitindoNegativo(despesas).
5. Testes em SumarioTest cobrindo os 9 ACs: lista vazia, receitas, despesas, aPagar, restante positivo e negativo, invariantes de pago/adição/remoção, determinismo. 100% de cobertura.
6. Validar com ./gradlew :server:test --tests 'br.dev.brunorsch.ledger.server.dominio.*' + jacocoTestReport.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validação: ./gradlew :server:test --tests 'br.dev.brunorsch.ledger.server.domain.*' → 44/44 passando. JaCoCo: Sumario 0/5, Sumario.Companion 0/13, Lancamento 0/5, TipoLancamento 0/1 — 100% de cobertura de linhas em todo o domínio. Suíte completa: 44/45 (única falha pré-existente em LedgerApplicationTests.contextLoads por falta de DB_URL).

Decisão: Restante pode ser negativo (AC #5), então adicionei Dinheiro.subtractAllowingNegative(outro) para preservar a invariante do subtract padrão (que rejeita negativo) e dar uma saída explícita para o caso do déficit. Cálculo em passada única somando receitas, despesas e aPagar; restante = receitas.subtractAllowingNegative(despesas).

Estrutura (um arquivo por classe): TipoLancamento.kt (enum DESPESA/RECEITA), Lancamento.kt (data class com valor, tipo, pago, descricao), Sumario.kt (data class com calcular no companion). Métodos de Dinheiro em inglês (sum/subtract/multiply/divide) consistentes com o resto do projeto. Campo pago do Lancamento é considerado só quando tipo = DESPESA; para RECEITA fica livre (documentado no KDoc).

Refactor 6 (review aplicado): (1) Lancamento virou sealed class com Lancamento.Receita(valor, descricao) e Lancamento.Despesa(valor, pago, descricao) — o flag pago só existe onde faz sentido, sem null-safety boilerplate. Sumario.calcular passou a iterar com when(is Lancamento.Receita / is Lancamento.Despesa) exaustivo. (2) link Lancamento→Mes: deixado para task-7 conforme decisão. (3) removido arquivo TipoLancamento.kt — enum virou desnecessário com a hierarquia. Estrutura final: Lancamento.kt (sealed + Receita + Despesa), Sumario.kt, Dinheiro.kt, DistribuicaoSobras.kt — 4 arquivos no pacote domain. 46/46 testes do domínio passando, 100% de cobertura mantido (Lancamento.Receita 0/3, Lancamento.Despesa 0/4, Sumario 0/5, Sumario.Companion 0/14, Dinheiro 0/9, Dinheiro.Companion 0/10, DistribuicaoSobras 0/9, DistribuicaoSobras.Companion 0/44).

Refactor 7 (AGENTS.md): aplicada a regra 'Separação de testes em Arrange, Act e Assert, mas sem explicitar com comentários. Deixando uma linha em branco para separar cada etapa' nos três arquivos de teste (SumarioTest, DinheiroTest, DistribuicaoSobrasTest). Cada teste agora agrupa setup, ação e asserções com linhas em branco; comentários sobre AC (que documentam qual critério está coberto) permanecem por serem documentação, não rótulos de fase. 46/46 testes do domínio passando, 100% de cobertura mantido.

Refactor 8 (review): renomeadas as variáveis curtas (s/d/d1/d2/sPago/sNaoPago/s1/s2 → resultado/resultado1/resultado2/resultadoPago/resultadoNaoPago) e migradas as asserts monetárias de assertEquals(0, x.compareTo(y)) para assertThat(x).isEqualByComparingTo(y) (AssertJ). Adicionada dependência org.assertj:assertj-core em server/build.gradle.kts (versão gerenciada pelo BOM do Spring Boot). Asserts não-monetárias (assertFailsWith, assertEquals em tipos não-Dinheiro, assertTrue booleano, assertFalse) mantidas do kotlin.test por serem mais ergonômicas naqueles casos. 46/46 testes do domínio passando, 100% de cobertura mantido.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Criado o cálculo do sumário em server/src/main/kotlin/br/dev/brunorsch/ledger/server/domain/Sumario.kt: data class Sumario(receitas, despesas, aPagar, restante) com factory Sumario.calcular(lancamentos) no companion. Tipos auxiliares Lancamento (data class) e TipoLancamento (enum DESPESA/RECEITA) em arquivos próprios. Restante negativo suportado via novo método Dinheiro.subtractAllowingNegative (com teste). 12 testes em SumarioTest cobrem os 9 ACs (lista vazia, receitas, despesas, aPagar, restante positivo/negativo, invariantes de pago/adição/remoção, determinismo) + 1 teste de cobertura para os métodos auto-gerados do data class Lancamento. 44/44 testes do domínio passam, JaCoCo 100% de cobertura em todo o pacote domain.
<!-- SECTION:FINAL_SUMMARY:END -->
