export function TelaCarregando() {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-line bg-paper py-24 shadow-sm">
      <div className="h-10 w-10 animate-spin rounded-full border-4 border-line border-t-accent" />
      <p className="mt-4 text-muted">Carregando mês...</p>
    </div>
  )
}
