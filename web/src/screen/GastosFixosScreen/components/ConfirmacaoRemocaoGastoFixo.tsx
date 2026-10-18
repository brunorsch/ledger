import { useRef, useEffect, useState } from 'react'
import { Botao } from '@/components'
import { ErroApi, type GastoFixoResponse } from '@/api/types'
import { useRemoverGastoFixo } from '@/api/queries'
import { formatarBRL } from '@/format/brl'

type ConfirmacaoRemocaoGastoFixoProps = {
  gastoFixo: GastoFixoResponse
  onSucesso: () => void
  onCancelar: () => void
}

export function ConfirmacaoRemocaoGastoFixo({
  gastoFixo,
  onSucesso,
  onCancelar,
}: ConfirmacaoRemocaoGastoFixoProps) {
  const remover = useRemoverGastoFixo()
  const botaoRef = useRef<HTMLButtonElement>(null)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    botaoRef.current?.focus()
  }, [])

  async function confirmar() {
    setErro(null)

    try {
      await remover.mutateAsync(gastoFixo.id)
      onSucesso()
    } catch (erroRemocao: unknown) {
      const mensagem =
        erroRemocao instanceof ErroApi
          ? erroRemocao.mensagem
          : 'Não foi possível remover o gasto fixo.'
      setErro(mensagem)
    }
  }

  return (
    <div className="space-y-5">
      <p className="text-sm text-muted">
        Tem certeza que deseja remover <strong className="text-ink">{gastoFixo.descricao}</strong> de{' '}
        <strong className="text-ink">{formatarBRL(gastoFixo.valorPadrao)}</strong>? Essa ação não pode
        ser desfeita.
      </p>

      {erro && (
        <p className="rounded-lg bg-danger-soft px-3 py-2 text-sm text-danger" role="alert">
          {erro}
        </p>
      )}

      <div className="flex flex-col-reverse gap-3 pt-2 sm:flex-row sm:justify-end">
        <Botao variante="secundaria" onClick={onCancelar} disabled={remover.isPending}>
          Cancelar
        </Botao>
        <Botao ref={botaoRef} variante="perigo" onClick={confirmar} disabled={remover.isPending}>
          {remover.isPending ? 'Removendo...' : 'Remover'}
        </Botao>
      </div>
    </div>
  )
}
