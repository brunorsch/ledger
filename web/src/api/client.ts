import { ErroApi } from '@/api/types'
import type {
  AlterarAlternanciasCorpo,
  AlterarGastoFixoCorpo,
  AlterarLancamentoCorpo,
  AlternanciasResponse,
  CriarGastoFixoCorpo,
  CriarLancamentoCorpo,
  GastoFixoResponse,
  LancamentoResponse,
  MesResponse,
  MesSumarioResponse,
} from '@/api/types'

const API_BASE = '/api/v1'

async function extrairMensagemErro(resposta: Response): Promise<string> {
  const status = resposta.status
  const corpo = await resposta.text().catch(() => '')

  if (corpo) {
    try {
      const json = JSON.parse(corpo) as { mensagem?: string }
      if (json.mensagem) return json.mensagem
    } catch {
      // Retorna o corpo textual bruto abaixo.
    }
    return corpo
  }

  return `Erro ${status} ao acessar ${resposta.url}`
}

async function requisicao<T>(url: string, opcoes?: RequestInit): Promise<T> {
  const resposta = await fetch(url, {
    ...opcoes,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...opcoes?.headers,
    },
  })

  if (!resposta.ok) {
    const mensagem = await extrairMensagemErro(resposta)
    throw new ErroApi(mensagem, resposta.status)
  }

  if (resposta.status === 204) {
    return undefined as T
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

export async function criarLancamento(
  mesId: string,
  corpo: CriarLancamentoCorpo
): Promise<LancamentoResponse> {
  return requisicao<LancamentoResponse>(`${API_BASE}/meses/${mesId}/lancamentos`, {
    method: 'POST',
    body: JSON.stringify(corpo),
  })
}

export async function alterarLancamento(
  id: string,
  corpo: AlterarLancamentoCorpo
): Promise<LancamentoResponse> {
  return requisicao<LancamentoResponse>(`${API_BASE}/lancamentos/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(corpo),
  })
}

export async function alternarPagoLancamento(
  id: string,
  pago: boolean
): Promise<LancamentoResponse> {
  return requisicao<LancamentoResponse>(`${API_BASE}/lancamentos/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ pago }),
  })
}

export async function removerLancamento(id: string): Promise<void> {
  return requisicao<void>(`${API_BASE}/lancamentos/${id}`, {
    method: 'DELETE',
  })
}

export async function alterarAlternancias(
  mesId: string,
  corpo: AlterarAlternanciasCorpo
): Promise<AlternanciasResponse> {
  return requisicao<AlternanciasResponse>(`${API_BASE}/meses/${mesId}/alternancias`, {
    method: 'PATCH',
    body: JSON.stringify(corpo),
  })
}

export async function listarGastosFixos(): Promise<GastoFixoResponse[]> {
  return requisicao<GastoFixoResponse[]>(`${API_BASE}/gastos-fixos`)
}

export async function criarGastoFixo(corpo: CriarGastoFixoCorpo): Promise<GastoFixoResponse> {
  return requisicao<GastoFixoResponse>(`${API_BASE}/gastos-fixos`, {
    method: 'POST',
    body: JSON.stringify(corpo),
  })
}

export async function alterarGastoFixo(
  id: string,
  corpo: AlterarGastoFixoCorpo
): Promise<GastoFixoResponse> {
  return requisicao<GastoFixoResponse>(`${API_BASE}/gastos-fixos/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(corpo),
  })
}

export async function removerGastoFixo(id: string): Promise<void> {
  return requisicao<void>(`${API_BASE}/gastos-fixos/${id}`, {
    method: 'DELETE',
  })
}
