import { initializeApp, type FirebaseApp } from 'firebase/app'
import { getAuth, type Auth } from 'firebase/auth'

function variavelAmbiente(nome: string): string {
  return (import.meta.env[nome] as string | undefined) ?? ''
}

const configuracaoFirebase = {
  apiKey: variavelAmbiente('VITE_FIREBASE_API_KEY'),
  authDomain: variavelAmbiente('VITE_FIREBASE_AUTH_DOMAIN'),
  projectId: variavelAmbiente('VITE_FIREBASE_PROJECT_ID'),
  appId: variavelAmbiente('VITE_FIREBASE_APP_ID'),
}

export const firebaseConfigurado = Object.values(configuracaoFirebase).every(
  (valor) => valor !== ''
)

if (!firebaseConfigurado) {
  console.warn(
    'Firebase não configurado: preencha VITE_FIREBASE_API_KEY, VITE_FIREBASE_AUTH_DOMAIN, ' +
      'VITE_FIREBASE_PROJECT_ID e VITE_FIREBASE_APP_ID no .env (veja .env.example). ' +
      'O login falhará até que os valores sejam informados.'
  )
}

export const firebaseApp: FirebaseApp = initializeApp(configuracaoFirebase)

export const autenticacao: Auth = getAuth(firebaseApp)
