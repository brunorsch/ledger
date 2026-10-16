---
id: decision-2
title: 'Adotar nomes de mercado sem traduzir (ex.: snapshot)'
date: '2026-10-16 22:35'
status: accepted
---
## Context

O código deve utilizar português para nomes relacionados ao domínio da aplicação, regras de negócio, entidades, variáveis, métodos, classes e comentários quando apropriado.

Entretanto, nem todo termo em inglês deve ser traduzido. A área de desenvolvimento possui um vocabulário técnico amplamente consolidado em inglês, inclusive entre desenvolvedores brasileiros. Traduções literais desses termos podem produzir código artificial ou alterar o significado técnico.

Por exemplo, `snapshot` é um termo técnico utilizado normalmente em português no contexto de desenvolvimento e infraestrutura. Traduzir `snapshot` como `captura` é uma tradução literal, mas não representa a terminologia normalmente utilizada por desenvolvedores brasileiros.

## Decision

O agente deve escrever o código em português, mas **preservar termos técnicos em inglês quando eles forem consagrados ou naturalmente utilizados pela comunidade de desenvolvimento brasileira**.

A regra é:

> **Traduzir conceitos e termos relacionados ao domínio da aplicação, mas não traduzir mecanicamente o vocabulário técnico da programação.**

Antes de traduzir um termo técnico, o agente deve avaliar se a tradução é realmente utilizada por desenvolvedores brasileiros.

Exemplos de termos que devem normalmente permanecer em inglês:

- `snapshot`
- `commit`
- `rollback`
- `deploy`
- `endpoint`
- `payload`
- `cache`
- `request`
- `response`
- `branch`
- `merge`
- `pipeline`
- `workflow`
- `build`
- `release`

Isso não impede traduções que já sejam naturais e consolidadas em português técnico, como `repository` → `repositório`, `user` → `usuário` ou `schedule` → `agendamento`.

Em caso de dúvida, deve-se preferir o termo que um desenvolvedor brasileiro utilizaria naturalmente em uma conversa técnica, documentação ou code review.

## Consequences

O código terá uma mistura intencional de português e inglês, refletindo o vocabulário realmente utilizado no desenvolvimento de software no Brasil.

Isso evita traduções literais e artificiais, preserva a precisão semântica dos termos técnicos e torna o código mais natural para desenvolvedores brasileiros.

Por outro lado, nem todos os identificadores estarão integralmente em português. Isso é esperado e **não deve ser considerado uma violação da regra de escrever código em português**.

O objetivo não é traduzir todo o inglês presente no código, mas produzir código que pareça ter sido escrito originalmente por um desenvolvedor brasileiro.