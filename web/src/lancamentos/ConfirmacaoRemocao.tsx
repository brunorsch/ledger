import { useRef, useEffect, useState } from 'react'
import { Botao } from '../componentes/Botao.tsx'
import { ErroApi, type LancamentoResponse } from '../api/tipos.ts'
import { useRemoverLancamento } from '../api/consultas.ts'
import { formatarBRL } from '../formato/brl.ts'

type ConfirmacaoRemocaoProps = {
  mesId: string
  lancamento: LancamentoResponse
  onSucesso: () => void
  onCancelar: () => void
}

export function ConfirmacaoRemocao({
  mesId,
  lancamento,
  onSucesso,
  onCancelar,
}: ConfirmacaoRemocaoProps) {
  const remover = useRemoverLancamento(mesId)
  const botaoRef = useRef<HTMLButtonElement>(null)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    botaoRef.current?.focus()
  }, [])

  async function confirmar() {
    setErro(null)

    try {
      await remover.mutateAsync(lancamento.id)
      onSucesso()
    } catch (erroRemocao) {
      const mensagem =
        erroRemocao instanceof ErroApi
          ? erroRemocao.mensagem
          : 'Não foi possível remover o lançamento.'
      setErro(mensagem)
    }
  }

  return (
    <div className="space-y-5">
      <p className="text-sm text-muted">
        Tem certeza que deseja remover <strong className="text-ink">{lancamento.descricao}</strong>{' '}
        de <strong className="text-ink">{formatarBRL(lancamento.valor)}</strong>? Essa ação não pode
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
        <Botao
          ref={botaoRef}
          variante="perigo"
          onClick={confirmar}
          disabled={remover.isPending}
        >
          {remover.isPending ? 'Removendo...' : 'Remover'}
        </Botao>
      </div>
    </div>
  )
}
