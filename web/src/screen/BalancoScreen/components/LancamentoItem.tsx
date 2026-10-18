import { useAlternarPagoLancamento } from '@/api/queries'
import { formatarBRL } from '@/format/brl'
import { Botao } from '@/components'
import type { LancamentoResponse } from '@/api/types'

type LancamentoItemProps = {
  mesId: string
  lancamento: LancamentoResponse
  onEditar: () => void
  onRemover: () => void
}

export function LancamentoItem({ mesId, lancamento, onEditar, onRemover }: LancamentoItemProps) {
  const isDespesa = lancamento.tipo === 'DESPESA'
  const isReceita = lancamento.tipo === 'RECEITA'
  const alternarPago = useAlternarPagoLancamento(mesId)

  async function aoAlternarPago() {
    if (!isDespesa) return
    try {
      await alternarPago.mutateAsync({ id: lancamento.id, pago: !lancamento.pago })
    } catch {
      // Em caso de erro nada é invalidado: a listagem mantém o status anterior.
    }
  }

  return (
    <div className="flex items-center justify-between gap-3 px-5 py-4 md:px-6">
      <div className="min-w-0">
        <div className="flex items-center gap-2">
          <span
            className={`inline-flex h-2 w-2 rounded-full ${isReceita ? 'bg-success' : 'bg-danger'}`}
            aria-hidden="true"
          />
          <p className="truncate font-medium">{lancamento.descricao}</p>
        </div>
        <p className="mt-1 text-xs text-subtle">
          {isDespesa ? (
            <button
              type="button"
              onClick={aoAlternarPago}
              disabled={alternarPago.isPending}
              aria-pressed={lancamento.pago}
              className={`inline-flex items-center gap-1 transition hover:underline disabled:cursor-not-allowed disabled:opacity-60 ${
                lancamento.pago ? 'text-success' : 'text-warning'
              }`}
            >
              {lancamento.pago ? (
                <>
                  <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  Pago
                </>
              ) : (
                <>Não pago</>
              )}
            </button>
          ) : (
            <span>Receita</span>
          )}
        </p>
      </div>

      <div className="flex shrink-0 items-center gap-3">
        <p
          className={`font-medium tabular-nums ${isReceita ? 'text-success' : 'text-ink'}`}
        >
          {isDespesa ? '-' : '+'}
          {formatarBRL(lancamento.valor)}
        </p>

        <div className="flex items-center gap-1">
          <Botao
            variante="fantasma"
            tamanho="pequeno"
            onClick={onEditar}
            aria-label={`Editar ${lancamento.descricao}`}
            className="h-8 w-8 p-0"
          >
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
            </svg>
          </Botao>
          <Botao
            variante="fantasma"
            tamanho="pequeno"
            onClick={onRemover}
            aria-label={`Remover ${lancamento.descricao}`}
            className="h-8 w-8 p-0 text-danger hover:bg-danger-soft hover:text-danger"
          >
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </Botao>
        </div>
      </div>
    </div>
  )
}
