---
id: TASK-22
title: Login no front com Firebase Authentication
status: Done
assignee:
  - '@brunorsch'
created_date: '2026-10-19 01:33'
updated_date: '2026-10-20 03:19'
labels:
  - frontend
  - auth
dependencies:
  - TASK-20
references:
  - backlog/tasks/task-20
modified_files:
  - web/package.json
  - web/pnpm-lock.yaml
  - web/.env.example
  - web/src/firebase/config.ts
  - web/src/auth/servicoAutenticacao.ts
  - web/src/auth/useAutenticacao.ts
  - web/src/api/client.ts
  - web/src/App.tsx
  - web/src/components/TelaCarregando.tsx
  - web/src/components/index.tsx
  - web/src/screen/LoginScreen/index.tsx
  - web/src/layout/AppLayout.tsx
  - web/src/screen/BalancoScreen/components/Header.tsx
  - web/src/screen/GastosFixosScreen/index.tsx
ordinal: 22000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
O backend já valida ID tokens do Firebase (TASK-20), mas o front ainda não tem login. Esta task adiciona o fluxo de autenticação no React: login com Google, Facebook e e-mail/senha via SDK do Firebase (lib aprovada pelo usuário), envio do ID token como Bearer em todas as chamadas, refresh automático via getIdToken, redirect para /login em 401 e logout com signOut. Config do SDK vem de env do Vite (VITE_FIREBASE_API_KEY, VITE_FIREBASE_AUTH_DOMAIN, VITE_FIREBASE_PROJECT_ID, VITE_FIREBASE_APP_ID — valores públicos; o usuário preenche os valores reais do projeto Firebase). Atenção dev: o perfil local do backend ignora o token (fallback X-Usuario-Id/seed); para testar o login real ponta a ponta, rodar o backend SEM o perfil local e com FIREBASE_PROJECT_ID definido. Sem testes de front (convenção do projeto); validação via pnpm lint/build e verificação manual.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Tela de login com Google, Facebook e e-mail/senha, coerente com o design do app (Tailwind, responsiva)
- [x] #2 Usuário logado fica em estado global (Zustand); rotas privadas redirecionam para /login quando não autenticado
- [x] #3 Todas as chamadas à API enviam o ID token como Bearer, renovado automaticamente via getIdToken
- [x] #4 Resposta 401 da API limpa o estado e redireciona para /login
- [x] #5 Logout chama signOut do SDK e volta para /login
- [x] #6 Configuração do Firebase vem de variáveis de ambiente do Vite (VITE_FIREBASE_*), sem segredos no repositório
- [x] #7 pnpm build e pnpm lint passam
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Fixer: instalar SDK firebase (app+auth); config por env VITE_FIREBASE_*; serviço de auth (login Google/Facebook/email, logout, observador de estado); store Zustand useAutenticacao; fetch wrapper com Bearer + getIdToken; guarda de rota e 401 -> /login; tela de login mínima funcional (placeholder estilizado depois).
2. Designer: UI/UX da tela de login e integração visual (botões dos provedores, formulário de e-mail/senha, estados de loading/erro), coerente com o design atual; revisão de copy.
3. Validação: pnpm lint e pnpm build; smoke manual do fluxo (com backend sem perfil local quando o usuário fornecer as VITE_FIREBASE_*).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Fixer: SDK firebase + zustand (não estava instalado), config VITE_FIREBASE_*, serviço de auth, store, Bearer + 401 -> sair+login, guard RequerAutenticacao, LoginScreen funcional e botão Sair. Designer: redesign da LoginScreen (marca, ícones SVG inline Google/Facebook, separador, banner de erro, loading por botão, aviso quando firebaseConfigurado=false), PerfilSidebar com identidade e Sair, Sair também no header de Gastos fixos, TelaCarregando compartilhada. pnpm lint e pnpm build verdes. Pendente: usuário preencher VITE_FIREBASE_* reais (a partir de .env.example) para testar o fluxo real; validação visual final do usuário (AC#1).

Ajustes do usuário após revisão visual (AC#1 aprovado por ele): retry:false nas queries de mes/sumario/lancamentos (evita o delay de ~1s do retry no loading), .env no .gitignore, e remoção do botão Sair do header do Balanço (permanece no header de Gastos fixos e na sidebar desktop; mobile no Balanço fica sem logout — homologando). pnpm lint e pnpm build verdes.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
TASK-22: login no front com Firebase Authentication. SDK firebase + Zustand; serviço de auth (Google/Facebook/email), store useAutenticacao com status/erro/usuario; Bearer automático via getIdToken; 401 -> sair + /login; guard RequerAutenticacao; tela de login redesenhada (marca, ícones SVG inline, loading por botão, banner de erro, aviso sem config); logout consistente (headers + perfil na sidebar); TelaCarregando compartilhada; config por VITE_FIREBASE_*. Verificado com pnpm lint e pnpm build + revisão visual do usuário. Release alvo: primeira semana de novembro.
<!-- SECTION:FINAL_SUMMARY:END -->
