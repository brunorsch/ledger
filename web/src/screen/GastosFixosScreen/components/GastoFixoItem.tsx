import { Botao } from '@/components'
import { formatarBRL } from '@/format/brl'
import type { GastoFixoResponse } from '@/api/types'
import type { EditarGastoFixoHandler, RemoverGastoFixoHandler } from './types'

type GastoFixoItemProps = {
  gastoFixo: GastoFixoResponse
  onEditar: EditarGastoFixoHandler
  onRemover: RemoverGastoFixoHandler
}

export function GastoFixoItem({ gastoFixo, onEditar, onRemover }: GastoFixoItemProps) {
  return (
    <div className="flex items-center justify-between gap-4 px-5 py-4 md:px-6">
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-ink">{gastoFixo.descricao}</p>
        <p className="mt-0.5 text-sm font-medium tabular-nums text-ink">
          {formatarBRL(gastoFixo.valorPadrao)}
        </p>
      </div>
      <div className="flex shrink-0 items-center gap-2">
        <Botao
          variante="fantasma"
          tamanho="pequeno"
          onClick={() => onEditar(gastoFixo)}
          aria-label={`Editar ${gastoFixo.descricao}`}
        >
          <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"
            />
          </svg>
        </Botao>
        <Botao
          variante="fantasma"
          tamanho="pequeno"
          onClick={() => onRemover(gastoFixo)}
          aria-label={`Remover ${gastoFixo.descricao}`}
        >
          <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
            />
          </svg>
        </Botao>
      </div>
    </div>
  )
}
