import { Box } from '@/components'
import { formatarBRL } from '@/format/brl'
import { formatarMesCurto } from '@/format/mes'
import type { MesSumarioResponse } from '@/api/types'

type BlocoDistribuicaoProps = {
  sumario: MesSumarioResponse
  mesReferencia: string
}

export function BlocoDistribuicao({ sumario, mesReferencia }: BlocoDistribuicaoProps) {
  const restanteNegativo = Number(sumario.restante) <= 0
  const distribuicao = sumario.distribuicao

  const itens: Array<{ rotulo: string; valor: string; destaque?: boolean }> = restanteNegativo
    ? [
        { rotulo: 'Essenciais', valor: distribuicao.essenciais },
        { rotulo: 'Lazer', valor: distribuicao.lazer },
        { rotulo: 'Reservas', valor: distribuicao.reservas },
        { rotulo: 'Déficit', valor: distribuicao.deficit, destaque: true },
      ]
    : [
        { rotulo: 'Essenciais', valor: distribuicao.essenciais },
        { rotulo: 'Lazer', valor: distribuicao.lazer },
        { rotulo: 'Reservas', valor: distribuicao.reservas },
      ]

  const nota = restanteNegativo
    ? undefined
    : `Baseado nos parâmetros de ${formatarMesCurto(mesReferencia)}.`

  return (
    <Box titulo="Distribuição das sobras" nota={nota}>
      <dl className="divide-y divide-line">
        {itens.map(({ rotulo, valor, destaque }) => (
          <div key={rotulo} className="flex items-center justify-between px-5 py-3.5 md:px-6">
            <dt className="text-sm text-muted">{rotulo}</dt>
            <dd className={`font-medium tabular-nums ${destaque ? 'text-danger' : 'text-ink'}`}>
              {formatarBRL(valor)}
            </dd>
          </div>
        ))}
      </dl>
    </Box>
  )
}
