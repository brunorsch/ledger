import { Botao } from '@/components'

type TelaErroProps = {
  mensagem: string
  onTentarNovamente: () => void
}

export function TelaErro({ mensagem, onTentarNovamente }: TelaErroProps) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-line bg-paper px-6 py-20 text-center shadow-sm">
      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-danger-soft text-danger">
        <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
      </div>
      <h2 className="mt-4 font-display text-lg font-medium">Algo deu errado</h2>
      <p className="mt-2 max-w-sm text-muted">{mensagem}</p>
      <Botao variante="primaria" onClick={onTentarNovamente} className="mt-6">
        Tentar novamente
      </Botao>
    </div>
  )
}
