import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  alternarPagoLancamento,
  alterarAlternancias,
  alterarGastoFixo,
  alterarLancamento,
  criarGastoFixo,
  criarLancamento,
  listarGastosFixos,
  listarLancamentos,
  obterMesPorReferencia,
  obterSumario,
  removerGastoFixo,
  removerLancamento,
} from '@/api/client'
import type {
  AlterarAlternanciasCorpo,
  AlterarGastoFixoCorpo,
  AlterarLancamentoCorpo,
  CriarGastoFixoCorpo,
  CriarLancamentoCorpo,
} from '@/api/types'

const chaves = {
  mes: (referencia: string) => ['mes', referencia] as const,
  sumario: (mesId: string) => ['sumario', mesId] as const,
  lancamentos: (mesId: string) => ['lancamentos', mesId] as const,
  gastosFixos: () => ['gastos-fixos'] as const,
}

export function useMes(referencia: string) {
  return useQuery({
    queryKey: chaves.mes(referencia),
    queryFn: () => obterMesPorReferencia(referencia),
  })
}

export function useSumario(mesId: string) {
  return useQuery({
    queryKey: chaves.sumario(mesId),
    queryFn: () => obterSumario(mesId),
    enabled: Boolean(mesId),
  })
}

export function useLancamentos(mesId: string) {
  return useQuery({
    queryKey: chaves.lancamentos(mesId),
    queryFn: () => listarLancamentos(mesId),
    enabled: Boolean(mesId),
  })
}

function useInvalidarMes(mesId: string) {
  const cliente = useQueryClient()

  return {
    invalidar: () => {
      cliente.invalidateQueries({ queryKey: chaves.sumario(mesId) })
      cliente.invalidateQueries({ queryKey: chaves.lancamentos(mesId) })
    },
  }
}

export function useCriarLancamento(mesId: string) {
  const { invalidar } = useInvalidarMes(mesId)

  return useMutation({
    mutationFn: (corpo: CriarLancamentoCorpo) => criarLancamento(mesId, corpo),
    onSuccess: invalidar,
  })
}

export function useAlterarLancamento(mesId: string) {
  const { invalidar } = useInvalidarMes(mesId)

  return useMutation({
    mutationFn: ({ id, corpo }: { id: string; corpo: AlterarLancamentoCorpo }) =>
      alterarLancamento(id, corpo),
    onSuccess: invalidar,
  })
}

export function useAlternarPagoLancamento(mesId: string) {
  const { invalidar } = useInvalidarMes(mesId)

  return useMutation({
    mutationFn: ({ id, pago }: { id: string; pago: boolean }) =>
      alternarPagoLancamento(id, pago),
    onSuccess: invalidar,
  })
}

export function useRemoverLancamento(mesId: string) {
  const { invalidar } = useInvalidarMes(mesId)

  return useMutation({
    mutationFn: (id: string) => removerLancamento(id),
    onSuccess: invalidar,
  })
}

export function useAlterarAlternancias(mesId: string) {
  const cliente = useQueryClient()

  return useMutation({
    mutationFn: (corpo: AlterarAlternanciasCorpo) => alterarAlternancias(mesId, corpo),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: chaves.sumario(mesId) })
      cliente.invalidateQueries({ queryKey: ['mes'] })
    },
  })
}

export function useGastosFixos() {
  return useQuery({
    queryKey: chaves.gastosFixos(),
    queryFn: listarGastosFixos,
  })
}

export function useCriarGastoFixo() {
  const cliente = useQueryClient()

  return useMutation({
    mutationFn: (corpo: CriarGastoFixoCorpo) => criarGastoFixo(corpo),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: chaves.gastosFixos() })
    },
  })
}

export function useAlterarGastoFixo() {
  const cliente = useQueryClient()

  return useMutation({
    mutationFn: ({ id, corpo }: { id: string; corpo: AlterarGastoFixoCorpo }) =>
      alterarGastoFixo(id, corpo),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: chaves.gastosFixos() })
    },
  })
}

export function useRemoverGastoFixo() {
  const cliente = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => removerGastoFixo(id),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: chaves.gastosFixos() })
    },
  })
}
