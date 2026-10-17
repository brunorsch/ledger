import { useId, type ButtonHTMLAttributes, type ReactNode, type Ref } from 'react'

type Variante = 'primaria' | 'secundaria' | 'perigo' | 'fantasma'

type BotaoProps = {
  variante?: Variante
  tamanho?: 'pequeno' | 'medio'
  children: ReactNode
  ref?: Ref<HTMLButtonElement>
} & ButtonHTMLAttributes<HTMLButtonElement>

const classesPorVariante: Record<Variante, string> = {
  primaria:
    'bg-ink text-paper hover:bg-accent focus-visible:outline-accent disabled:bg-subtle',
  secundaria:
    'border border-line bg-paper text-ink hover:border-ink/20 hover:bg-fill focus-visible:outline-accent disabled:text-subtle',
  perigo:
    'bg-danger text-paper hover:bg-danger/90 focus-visible:outline-danger disabled:bg-subtle',
  fantasma:
    'bg-transparent text-muted hover:text-ink hover:bg-fill focus-visible:outline-accent disabled:text-subtle',
}

const classesPorTamanho: Record<'pequeno' | 'medio', string> = {
  pequeno: 'px-3 py-1.5 text-xs',
  medio: 'px-4 py-2.5 text-sm',
}

export function Botao({
  variante = 'primaria',
  tamanho = 'medio',
  className = '',
  children,
  ref,
  ...resto
}: BotaoProps) {
  const id = useId()

  return (
    <button
      id={id}
      ref={ref}
      type="button"
      className={`inline-flex items-center justify-center gap-2 rounded-lg font-medium transition focus-visible:outline-2 focus-visible:outline-offset-2 disabled:cursor-not-allowed ${classesPorVariante[variante]} ${classesPorTamanho[tamanho]} ${className}`}
      {...resto}
    >
      {children}
    </button>
  )
}
