import type { LancamentoResponse, MesResponse, MesSumarioResponse } from '@/api/types'
import { CardLancamentos } from './CardLancamentos'
import { BlocoSumario } from './BlocoSumario'
import { BlocoDistribuicao } from './BlocoDistribuicao'
import { BlocoConfiguracoes } from './BlocoConfiguracoes'
import type {
  CriarLancamentoHandler,
  EditarLancamentoHandler,
  RemoverLancamentoHandler,
} from './types'

type TelaProntoProps = {
  mes: MesResponse
  sumario: MesSumarioResponse
  lancamentos: LancamentoResponse[]
  onCriar: CriarLancamentoHandler
  onEditar: EditarLancamentoHandler
  onRemover: RemoverLancamentoHandler
}

export function TelaPronto({
  mes,
  sumario,
  lancamentos,
  onCriar,
  onEditar,
  onRemover,
}: TelaProntoProps) {
  const despesas = lancamentos.filter((l) => l.tipo === 'DESPESA')
  const receitas = lancamentos.filter((l) => l.tipo === 'RECEITA')

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <section className="space-y-6 lg:col-span-2">
        <CardLancamentos
          titulo="Despesas"
          lancamentos={despesas}
          destaque
          mensagemVazia="Nenhuma despesa neste mês."
          acaoCriar={{ rotulo: 'Adicionar despesa', onClick: () => onCriar('DESPESA') }}
          mesId={mes.id}
          onEditar={onEditar}
          onRemover={onRemover}
        />
        <CardLancamentos
          titulo="Receitas"
          lancamentos={receitas}
          mensagemVazia="Nenhuma receita neste mês."
          acaoCriar={{ rotulo: 'Adicionar receita', onClick: () => onCriar('RECEITA') }}
          mesId={mes.id}
          onEditar={onEditar}
          onRemover={onRemover}
        />
      </section>

      <aside className="space-y-6">
        <BlocoSumario sumario={sumario} />
        <BlocoDistribuicao sumario={sumario} mesReferencia={mes.mesReferencia} />
        <BlocoConfiguracoes mes={mes} />
      </aside>
    </div>
  )
}
