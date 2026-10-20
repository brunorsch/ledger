import {
  FacebookAuthProvider,
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  type User,
  type UserCredential,
} from 'firebase/auth'
import { autenticacao } from '@/firebase/config'

export function entrarComGoogle(): Promise<UserCredential> {
  return signInWithPopup(autenticacao, new GoogleAuthProvider())
}

export function entrarComFacebook(): Promise<UserCredential> {
  return signInWithPopup(autenticacao, new FacebookAuthProvider())
}

export function entrarComEmailSenha(email: string, senha: string): Promise<UserCredential> {
  return signInWithEmailAndPassword(autenticacao, email, senha)
}

export function sair(): Promise<void> {
  return signOut(autenticacao)
}

export function observarMudancaAutenticacao(
  callback: (usuario: User | null) => void
): () => void {
  return onAuthStateChanged(autenticacao, callback)
}

export async function obterIdToken(): Promise<string | null> {
  const usuario = autenticacao.currentUser
  if (!usuario) return null
  return usuario.getIdToken()
}
