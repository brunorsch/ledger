import { useId, type ChangeEvent, type InputHTMLAttributes, type SyntheticEvent } from 'react'
import { formatarBRL } from '@/format/brl'
import { extrairCentavos, valorDosCentavos } from '@/format/valor'

// Limite do NUMERIC(14,2) no banco.
const MAXIMO_DIGITOS = 14

type CampoValorProps = {
  label: string
  centavos: string
  onAlterar: (centavos: string) => void
  erro?: string
} & Omit<InputHTMLAttributes<HTMLInputElement>, 'value' | 'onChange' | 'inputMode'>

export function CampoValor({ label, centavos, onAlterar, erro, ...resto }: CampoValorProps) {
  const id = useId()
  const idErro = `${id}-erro`

  function aoAlterar(evento: ChangeEvent<HTMLInputElement>) {
    const novosCentavos = extrairCentavos(evento.target.value)
    if (novosCentavos.length > MAXIMO_DIGITOS) return

    onAlterar(novosCentavos)
  }

  // Com o cursor sempre no fim, os dígitos entram pela direita e apagar remove o último.
  // Uma seleção de texto é mantida para permitir selecionar tudo e digitar por cima.
  function manterCursorNoFim(evento: SyntheticEvent<HTMLInputElement>) {
    const campo = evento.currentTarget
    if (campo.selectionStart !== campo.selectionEnd) return

    const fim = campo.value.length
    campo.setSelectionRange(fim, fim)
  }

  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm font-medium text-ink">
        {label}
      </label>
      <input
        id={id}
        inputMode="numeric"
        value={formatarBRL(valorDosCentavos(centavos))}
        onChange={aoAlterar}
        onSelect={manterCursorNoFim}
        aria-invalid={Boolean(erro)}
        aria-describedby={erro ? idErro : undefined}
        className={`w-full rounded-lg border bg-paper px-3 py-2.5 text-sm outline-none transition placeholder:text-subtle focus:border-accent focus:ring-2 focus:ring-accent/10 ${
          erro ? 'border-danger focus:border-danger focus:ring-danger/10' : 'border-line'
        }`}
        {...resto}
      />
      {erro && (
        <p id={idErro} className="text-xs text-danger">
          {erro}
        </p>
      )}
    </div>
  )
}
