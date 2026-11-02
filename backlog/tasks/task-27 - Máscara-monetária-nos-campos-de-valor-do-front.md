---
id: TASK-27
title: Máscara monetária nos campos de valor do front
status: To Do
assignee: []
created_date: '2026-11-01 06:52'
labels:
  - bug
  - frontend
dependencies: []
references:
  - web/src/format/valor.ts
priority: high
type: bug
ordinal: 27000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Hoje `normalizarValorMonetario` (`web/src/format/valor.ts`) remove todos os pontos antes de interpretar o valor: "12.50" vira R$ 1.250,00 e "0.99" vira R$ 99,00, embora a mensagem de erro diga que vírgula ou ponto são aceitos. Assim, o usuário pode gravar um lançamento 100 vezes maior sem perceber. Decisão do usuário (2026-11-01): trocar a digitação livre por um campo com máscara, digitado sempre em centavos e exibido já formatado. Restrições do AGENTS.md: o front não faz conta de dinheiro e envia os valores à API como string; uma lib de máscara nova precisa de aprovação do usuário.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Os campos de valor do lançamento, do gasto fixo e dos tetos de criar mês exibem o valor sempre formatado em BRL (ex.: R$ 1.234,56) enquanto o usuário digita
- [ ] #2 A digitação é em centavos: os dígitos entram pela direita (digitar 1, 2, 5, 0 mostra R$ 12,50; digitar 9, 9 mostra R$ 0,99)
- [ ] #3 Caracteres que não são dígitos são ignorados e apagar remove o último dígito
- [ ] #4 O valor enviado à API é a string com duas casas decimais do que está na tela (R$ 1.234,56 → "1234.56"), sem conversão por ponto flutuante
- [ ] #5 Ao editar um lançamento ou gasto fixo, e no pré-preenchimento de criar mês, o campo abre com o valor atual já na máscara
- [ ] #6 Lançamento e gasto fixo continuam exigindo valor maior que zero, e os tetos continuam aceitando zero
- [ ] #7 As mensagens de erro deixam de citar "vírgula ou ponto"
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Verificado com `pnpm --dir web lint`, `pnpm --dir web exec tsc -b` e teste manual (o projeto não tem testes de front)
<!-- DOD:END -->
