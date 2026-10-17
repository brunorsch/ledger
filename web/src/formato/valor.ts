export function normalizarValorMonetario(valor: string): string | null {
  const limpo = valor.trim().replace(/\./g, '').replace(',', '.')

  if (!/^\d+(\.\d{1,2})?$/.test(limpo)) return null

  const numero = Number(limpo)
  if (Number.isNaN(numero) || numero <= 0) return null

  const [inteiro, decimal = ''] = limpo.split('.')
  const decimalAjustado = decimal.padEnd(2, '0')

  return `${inteiro}.${decimalAjustado}`
}
