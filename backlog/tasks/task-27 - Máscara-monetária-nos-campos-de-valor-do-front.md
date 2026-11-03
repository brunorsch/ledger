---
id: TASK-27
title: Máscara monetária nos campos de valor do front
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-11-01 06:52'
updated_date: '2026-11-03 02:13'
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
- [x] #1 Os campos de valor do lançamento, do gasto fixo e dos tetos de criar mês exibem o valor sempre formatado em BRL (ex.: R$ 1.234,56) enquanto o usuário digita
- [x] #2 A digitação é em centavos: os dígitos entram pela direita (digitar 1, 2, 5, 0 mostra R$ 12,50; digitar 9, 9 mostra R$ 0,99)
- [x] #3 Caracteres que não são dígitos são ignorados e apagar remove o último dígito
- [x] #4 O valor enviado à API é a string com duas casas decimais do que está na tela (R$ 1.234,56 → "1234.56"), sem conversão por ponto flutuante
- [x] #5 Ao editar um lançamento ou gasto fixo, e no pré-preenchimento de criar mês, o campo abre com o valor atual já na máscara
- [x] #6 Lançamento e gasto fixo continuam exigindo valor maior que zero, e os tetos continuam aceitando zero
- [x] #7 As mensagens de erro deixam de citar "vírgula ou ponto"
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [x] #1 Verificado com `pnpm --dir web lint`, `pnpm --dir web exec tsc -b` e teste manual (o projeto não tem testes de front)
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Reescrever web/src/format/valor.ts com extrairCentavos, centavosDoValor e valorDosCentavos (só operações de string, sem Number).
2. CampoValor com props centavos/onAlterar: exibe formatarBRL(valorDosCentavos(centavos)), sempre R$ 0,00 quando zero, ignora não dígitos, limite de 14 dígitos (NUMERIC(14,2)), cursor mantido no fim via onSelect, inputMode numeric.
3. FormularioLancamento e FormularioGastoFixo: estado em centavos, validação !centavos com 'Informe um valor maior que zero.', envio com valorDosCentavos.
4. FormularioCriarMes e BalancoScreen: tetos em centavos, pré-preenchimento com centavosDoValor, remoção da validação de tetos (zero aceito).
5. Verificar com pnpm --dir web lint, pnpm --dir web exec tsc -b, script descartável das funções de valor.ts e roteiro de teste manual para o usuário.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Máscara sem lib nova. O estado dos formulários são centavos como string de dígitos sem zeros à esquerda; valor.ts converte de/para o formato da API só com operações de string. CampoValor mostra sempre o valor formatado (R$ 0,00 quando zero, decisão do usuário), ignora não dígitos, limita a 14 dígitos (NUMERIC(14,2)) e mantém o cursor no fim via onSelect; selecionar tudo e digitar ou apagar por cima continua funcionando. Um colar com mais de 14 dígitos é descartado inteiro. A validação dos tetos em criar mês saiu, porque com a máscara todo estado é válido e zero é aceito.

Verificação:
- pnpm --dir web lint e pnpm --dir web exec tsc -b: OK.
- Script descartável (node --experimental-strip-types) com asserts de extrairCentavos, centavosDoValor e valorDosCentavos: 1,2,5,0 → R$ 12,50 / 12.50; 9,9 → R$ 0,99; 1234.5 da API → R$ 1.234,50; idas e voltas sem perda; limite de 14 dígitos.
- CampoValor numa página temporária do Vite (já removida), no navegador: digitação pela direita; letras e separadores ignorados; Backspace remove o último dígito, inclusive com o cursor movido para o meio; Home + dígito entra pela direita; Ctrl+A + dígito substitui; Ctrl+A + Backspace → R$ 0,00; colar 1.234,56 → R$ 1.234,56; para em 14 dígitos; console sem erros.
- Pendente: teste manual no app de verdade (login Firebase), cobrindo os AC 4 (payload), 5 (abrir edição/pré-preenchimento) e 6 (validação > 0 e teto zero).

Teste manual no app feito pelo usuário (2026-11-02): payload com duas casas, edição e pré-preenchimento abrindo na máscara, valor zero rejeitado em lançamento e gasto fixo, teto zero aceito. Tudo ok.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Troca a digitação livre dos campos de valor por uma máscara em centavos, sem lib nova. Corrige o bug em que "12.50" virava R$ 1.250,00.

- web/src/format/valor.ts: saiu normalizarValorMonetario. Entraram extrairCentavos, centavosDoValor e valorDosCentavos, que convertem só com operações de string, sem ponto flutuante.
- CampoValor: recebe centavos e onAlterar e mostra sempre o valor em BRL (R$ 0,00 quando zero). Ignora não dígitos, mantém o cursor no fim para os dígitos entrarem pela direita, limita a 14 dígitos (NUMERIC(14,2)) e usa inputMode numeric.
- Lançamento e gasto fixo: estado em centavos, validação > 0 com 'Informe um valor maior que zero.' e envio por valorDosCentavos.
- Criar mês: tetos em centavos, pré-preenchidos com centavosDoValor; saiu a validação dos tetos, porque zero é aceito e todo estado da máscara é válido.

Verificação: pnpm --dir web lint e pnpm --dir web exec tsc -b OK; script de asserts das conversões; CampoValor exercitado no navegador numa página temporária do Vite; teste manual do usuário no app, tudo ok.
<!-- SECTION:FINAL_SUMMARY:END -->
