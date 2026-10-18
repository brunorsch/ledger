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

export function mesAtual(): string {
  const hoje = new Date()
  const ano = hoje.getFullYear()
  const mes = String(hoje.getMonth() + 1).padStart(2, '0')
  return `${ano}-${mes}`
}

export function somarMeses(referencia: string, delta: number): string {
  const [ano, mes] = referencia.split('-').map(Number)
  const data = new Date(ano, mes - 1 + delta, 1)
  const anoResultado = data.getFullYear()
  const mesResultado = String(data.getMonth() + 1).padStart(2, '0')
  return `${anoResultado}-${mesResultado}`
}
