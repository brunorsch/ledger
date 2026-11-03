// Centavos são uma string só de dígitos, sem zeros à esquerda: "" é zero e "1250" é R$ 12,50.
// As conversões usam só operações de string para não passar por ponto flutuante.

export function extrairCentavos(texto: string): string {
  return texto.replace(/\D/g, '').replace(/^0+/, '')
}

export function centavosDoValor(valor: string): string {
  const [inteiro, decimal = ''] = valor.split('.')
  return extrairCentavos(inteiro + decimal.padEnd(2, '0'))
}

export function valorDosCentavos(centavos: string): string {
  const digitos = centavos.padStart(3, '0')
  return `${digitos.slice(0, -2)}.${digitos.slice(-2)}`
}
