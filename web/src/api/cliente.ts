import { ErroApi } from './tipos.ts'
import type { LancamentoResponse, MesResponse, MesSumarioResponse } from './tipos.ts'

const API_BASE = '/api/v1'

async function requisicao<T>(url: string, opcoes?: RequestInit): Promise<T> {
  const resposta = await fetch(url, {
    ...opcoes,
    headers: {
      Accept: 'application/json',
      ...opcoes?.headers,
    },
  })

  if (!resposta.ok) {
    const status = resposta.status
    const corpo = await resposta.text().catch(() => '')
    const mensagem = corpo || `Erro ${status} ao acessar ${url}`
    throw new ErroApi(mensagem, status)
  }

  return resposta.json() as Promise<T>
}

export async function obterMesPorReferencia(referencia: string): Promise<MesResponse> {
  return requisicao<MesResponse>(`${API_BASE}/meses?referencia=${encodeURIComponent(referencia)}`)
}

export async function obterSumario(mesId: string): Promise<MesSumarioResponse> {
  return requisicao<MesSumarioResponse>(`${API_BASE}/meses/${mesId}/sumario`)
}

export async function listarLancamentos(mesId: string): Promise<LancamentoResponse[]> {
  return requisicao<LancamentoResponse[]>(`${API_BASE}/meses/${mesId}/lancamentos`)
}
