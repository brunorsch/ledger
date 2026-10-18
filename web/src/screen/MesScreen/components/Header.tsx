import { formatarTituloMes } from "@/format/mes"

type HeaderProps = {
  referencia: string
}

export function Header({ referencia }: HeaderProps) {
  return (
    <header className="mb-8 md:mb-10">
      <div className="flex items-center justify-between rounded-t-2xl border-b border-line bg-paper px-4 py-3 md:px-6 md:py-4">
        <div className="flex items-center gap-2.5">
          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-accent font-display text-sm font-semibold text-white md:h-8 md:w-8 md:text-base">
            L
          </span>
          <span className="font-display text-sm font-semibold tracking-tight text-ink md:text-base">
            Ledger
          </span>
        </div>
        <p className="text-xs font-medium uppercase tracking-wide text-muted md:text-sm">
          Controle financeiro
        </p>
      </div>
      <div className="rounded-b-2xl border border-t-0 border-line bg-paper px-4 py-5 md:px-6 md:py-6">
        <h1 className="font-display text-3xl font-semibold tracking-tight text-ink md:text-4xl">
          {formatarTituloMes(referencia)}
        </h1>
      </div>
    </header>
  )
}
