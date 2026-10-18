import { useId, type InputHTMLAttributes } from 'react'

type CampoTextoProps = {
  label: string
  erro?: string
} & InputHTMLAttributes<HTMLInputElement>

export function CampoTexto({ label, erro, ...resto }: CampoTextoProps) {
  const id = useId()
  const idErro = `${id}-erro`

  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm font-medium text-ink">
        {label}
      </label>
      <input
        id={id}
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
