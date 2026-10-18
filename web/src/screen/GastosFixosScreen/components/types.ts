import type { GastoFixoResponse } from '@/api/types'

export type ModalAtivoGastoFixo =
  | { tipo: 'criar' }
  | { tipo: 'editar'; gastoFixo: GastoFixoResponse }
  | { tipo: 'remover'; gastoFixo: GastoFixoResponse }
  | null

export type EditarGastoFixoHandler = (gastoFixo: GastoFixoResponse) => void
export type RemoverGastoFixoHandler = (gastoFixo: GastoFixoResponse) => void
