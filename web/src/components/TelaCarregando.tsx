type TelaCarregandoProps = {
  mensagem?: string
}

export function TelaCarregando({ mensagem }: TelaCarregandoProps) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-fill px-4">
      <div className="h-10 w-10 animate-spin rounded-full border-4 border-line border-t-accent" />
      {mensagem && <p className="mt-4 text-sm text-muted">{mensagem}</p>}
    </div>
  )
}
