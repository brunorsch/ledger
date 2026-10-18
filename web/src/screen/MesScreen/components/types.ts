import type { LancamentoResponse } from '@/api/types'

export type ModalAtivo =
  | { tipo: 'criar'; tipoLancamento: 'DESPESA' | 'RECEITA' }
  | { tipo: 'editar'; lancamento: LancamentoResponse }
  | { tipo: 'remover'; lancamento: LancamentoResponse }
  | null

export type CriarLancamentoHandler = (tipo: 'DESPESA' | 'RECEITA') => void
export type EditarLancamentoHandler = (lancamento: LancamentoResponse) => void
export type RemoverLancamentoHandler = (lancamento: LancamentoResponse) => void
