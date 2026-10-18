import { Box } from '@/components'
import { formatarBRL } from '@/format/brl'
import type { MesSumarioResponse } from '@/api/types'

type BlocoSumarioProps = {
  sumario: MesSumarioResponse
}

export function BlocoSumario({ sumario }: BlocoSumarioProps) {
  const itens: Array<{
    rotulo: string
    valor: string
    positivo?: boolean
    destaque?: boolean
  }> = [
    { rotulo: 'Receitas', valor: sumario.receitas, positivo: true },
    { rotulo: 'Despesas', valor: sumario.despesas },
    { rotulo: 'A pagar', valor: sumario.aPagar },
    { rotulo: 'Restante', valor: sumario.restante, destaque: true },
  ]

  return (
    <Box titulo="Sumário">
      <dl className="divide-y divide-line">
        {itens.map(({ rotulo, valor, positivo, destaque }) => (
          <div key={rotulo} className="flex items-center justify-between px-5 py-3.5 md:px-6">
            <dt className="text-sm text-muted">{rotulo}</dt>
            <dd
              className={`font-medium tabular-nums ${
                destaque
                  ? Number(valor) >= 0
                    ? 'text-success'
                    : 'text-danger'
                  : positivo
                    ? 'text-success'
                    : 'text-ink'
              }`}
            >
              {formatarBRL(valor)}
            </dd>
          </div>
        ))}
      </dl>
    </Box>
  )
}
