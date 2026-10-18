const formatador = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
})

export function formatarBRL(valor: string): string {
  return formatador.format(Number(valor))
}
