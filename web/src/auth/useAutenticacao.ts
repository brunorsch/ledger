import { create } from 'zustand'
import type { FirebaseError } from 'firebase/app'
import {
  entrarComEmailSenha,
  entrarComFacebook,
  entrarComGoogle,
  observarMudancaAutenticacao,
  sair as sairDoFirebase,
} from '@/auth/servicoAutenticacao'

export type StatusAutenticacao = 'carregando' | 'autenticado' | 'deslogado'

export type UsuarioAutenticado = {
  nome: string | null
  email: string | null
}

type EstadoAutenticacao = {
  status: StatusAutenticacao
  erro: string | null
  usuario: UsuarioAutenticado | null
  entrarComGoogle: () => Promise<void>
  entrarComFacebook: () => Promise<void>
  entrarComEmail: (email: string, senha: string) => Promise<void>
  sair: () => Promise<void>
}

const MENSAGENS_ERRO: Record<string, string> = {
  'auth/user-not-found': 'E-mail ou senha incorretos',
  'auth/wrong-password': 'E-mail ou senha incorretos',
  'auth/invalid-credential': 'E-mail ou senha incorretos',
  'auth/invalid-email': 'E-mail inválido',
  'auth/user-disabled': 'Esta conta foi desativada',
  'auth/too-many-requests': 'Muitas tentativas. Tente novamente mais tarde',
  'auth/account-exists-with-different-credential':
    'Já existe uma conta com este e-mail usando outro método de login',
  'auth/popup-blocked': 'O navegador bloqueou a janela de login. Permita pop-ups e tente novamente',
  'auth/network-request-failed': 'Falha de conexão. Verifique sua internet',
  'auth/operation-not-allowed': 'Este método de login não está habilitado',
  'auth/unauthorized-domain': 'Domínio não autorizado no Firebase',
}

const CODIGOS_SILENCIOSOS = new Set(['auth/popup-closed-by-user', 'auth/cancelled-popup-request'])

function mensagemDeErro(erro: unknown): string | null {
  const codigo = (erro as FirebaseError | undefined)?.code
  if (codigo && CODIGOS_SILENCIOSOS.has(codigo)) return null
  if (codigo && MENSAGENS_ERRO[codigo]) return MENSAGENS_ERRO[codigo]
  return 'Não foi possível entrar. Tente novamente'
}

export const useAutenticacao = create<EstadoAutenticacao>((set) => {
  observarMudancaAutenticacao((usuario) => {
    set({
      status: usuario ? 'autenticado' : 'deslogado',
      erro: null,
      usuario: usuario
        ? { nome: usuario.displayName, email: usuario.email }
        : null,
    })
  })

  async function executarLogin(acao: () => Promise<unknown>): Promise<void> {
    set({ erro: null })
    try {
      await acao()
    } catch (erro) {
      const mensagem = mensagemDeErro(erro)
      if (mensagem) set({ erro: mensagem })
    }
  }

  return {
    status: 'carregando',
    erro: null,
    usuario: null,
    entrarComGoogle: () => executarLogin(entrarComGoogle),
    entrarComFacebook: () => executarLogin(entrarComFacebook),
    entrarComEmail: (email, senha) => executarLogin(() => entrarComEmailSenha(email, senha)),
    sair: async () => {
      set({ erro: null })
      await sairDoFirebase()
    },
  }
})
