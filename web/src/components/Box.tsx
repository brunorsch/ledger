import type { ReactNode } from 'react'

type BoxProps = {
  titulo: string
  contagem?: number
  nota?: string
  destaque?: boolean
  acao?: ReactNode
  children: ReactNode
}

export function Box({ titulo, contagem, nota, destaque = false, acao, children }: BoxProps) {
  return (
    <div
      className={`rounded-2xl border bg-paper shadow-sm ${
        destaque ? 'border-ink/10' : 'border-line'
      }`}
    >
      <div
        className={`border-b px-5 py-4 md:px-6 ${
          destaque ? 'border-ink/10 bg-ink/[0.02]' : 'border-line'
        }`}
      >
        <div className="flex items-center justify-between gap-3">
          <div className="flex min-w-0 items-center gap-2">
            <h2 className="font-display text-lg font-medium">{titulo}</h2>
            {contagem !== undefined && (
              <span className="rounded-full bg-fill px-2.5 py-1 text-xs font-medium text-muted">
                {contagem}
              </span>
            )}
          </div>
          {acao && <div className="shrink-0">{acao}</div>}
        </div>
        {nota && <p className="mt-1 text-xs text-muted">{nota}</p>}
      </div>
      {children}
    </div>
  )
}
