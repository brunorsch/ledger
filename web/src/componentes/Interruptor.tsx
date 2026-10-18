import { useId, type InputHTMLAttributes } from 'react'

type InterruptorProps = {
  rotulo: string
  descricao?: string
  checked: boolean
  disabled?: boolean
  onChange: (checked: boolean) => void
} & Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'checked' | 'onChange'>

export function Interruptor({
  rotulo,
  descricao,
  checked,
  disabled = false,
  onChange,
  ...resto
}: InterruptorProps) {
  const id = useId()

  return (
    <label
      htmlFor={id}
      className={`flex min-h-[44px] items-center justify-between gap-4 py-1 ${disabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'}`}
    >
      <span className="flex flex-col">
        <span className="text-sm font-medium text-ink">{rotulo}</span>
        {descricao && <span className="text-xs text-muted">{descricao}</span>}
      </span>

      <span className="relative inline-flex shrink-0">
        <input
          id={id}
          type="checkbox"
          role="switch"
          aria-checked={checked}
          checked={checked}
          disabled={disabled}
          onChange={(evento) => onChange(evento.target.checked)}
          className="peer sr-only"
          {...resto}
        />
        <span
          aria-hidden="true"
          className="block h-7 w-12 rounded-full border border-line bg-fill transition peer-checked:border-accent peer-checked:bg-accent peer-focus-visible:ring-2 peer-focus-visible:ring-accent peer-focus-visible:ring-offset-2 peer-disabled:cursor-not-allowed"
        />
        <span
          aria-hidden="true"
          className="pointer-events-none absolute left-0.5 top-0.5 h-6 w-6 rounded-full border border-line/50 bg-paper shadow-sm transition-transform peer-checked:translate-x-5 peer-checked:border-paper peer-disabled:cursor-not-allowed"
        />
      </span>
    </label>
  )
}
