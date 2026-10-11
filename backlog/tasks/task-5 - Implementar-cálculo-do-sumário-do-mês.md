---
id: TASK-5
title: Implementar cálculo do sumário do mês
status: To Do
assignee: []
created_date: '2026-10-11 17:37'
updated_date: '2026-10-11 17:46'
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
- [ ] #1 Mês sem lançamentos retorna `Receitas = Despesas = A pagar = Restante = 0`
- [ ] #2 `Receitas` soma todos os lançamentos de receita do mês em `Dinheiro`
- [ ] #3 `Despesas` soma todos os lançamentos de despesa (pagas e não pagas)
- [ ] #4 `A pagar` soma apenas despesas com flag `pago = false`
- [ ] #5 `Restante = Receitas − Despesas`, podendo ser negativo quando despesas superam receitas
- [ ] #6 Alterar o status pago de uma despesa recalcula `A pagar` (teste de invariante explícito)
- [ ] #7 Remover ou adicionar um lançamento recalcula o sumário (teste de invariante explícito)
- [ ] #8 Sumário é determinístico: mesmos lançamentos produzem exatamente o mesmo resultado
- [ ] #9 Cobertura de testes unitários alcança 100% das linhas do cálculo de sumário
<!-- AC:END -->
