export function formatarTituloMes(referencia: string): string {
  const [ano, mes] = referencia.split('-').map(Number)
  const data = new Date(ano, mes - 1, 1)
  return data.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
}

export function formatarMesCurto(referencia: string): string {
  const [ano, mes] = referencia.split('-').map(Number)
  const data = new Date(ano, mes - 1, 1)
  return data.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')
}
