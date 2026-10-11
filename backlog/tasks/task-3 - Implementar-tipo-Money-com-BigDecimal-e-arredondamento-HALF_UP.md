---
id: TASK-3
title: Implementar tipo Dinheiro com BigDecimal e arredondamento HALF_UP
status: To Do
assignee: []
created_date: '2026-10-11 17:37'
updated_date: '2026-10-11 17:45'
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
- [ ] #1 `Dinheiro("1193.08")` e `Dinheiro(BigDecimal("1193.080"))` representam o mesmo valor e `compareTo == 0`
- [ ] #2 Dois `Dinheiro` com mesma grandeza e escalas diferentes (ex.: `2.0` vs `2.00`) têm `compareTo == 0` mas `equals == false` (comportamento documentado)
- [ ] #3 `Dinheiro("0.125").somar(Dinheiro("0"))` resulta em `Dinheiro("0.13")` após HALF_UP
- [ ] #4 Construtor padrão rejeita valor negativo com exceção explícita
- [ ] #5 Factory explícita para valor negativo (`Dinheiro.allowNegative`) é usada na representação do déficit do mês
- [ ] #6 `Dinheiro.ZERO` é um singleton igual a qualquer `Dinheiro("0")` por `compareTo`
- [ ] #7 Cobertura de testes unitários alcança 100% das linhas do value class
<!-- AC:END -->
