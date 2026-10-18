import { Botao, Box } from '@/components'
import type { LancamentoResponse } from '@/api/types'
import { LancamentoItem } from './LancamentoItem'

type AcaoCriar = {
  rotulo: string
  onClick: () => void
}

type CardLancamentosProps = {
  titulo: string
  lancamentos: LancamentoResponse[]
  destaque?: boolean
  mensagemVazia: string
  acaoCriar: AcaoCriar
  mesId: string
  onEditar: (lancamento: LancamentoResponse) => void
  onRemover: (lancamento: LancamentoResponse) => void
}

export function CardLancamentos({
  titulo,
  lancamentos,
  destaque = false,
  mensagemVazia,
  acaoCriar,
  mesId,
  onEditar,
  onRemover,
}: CardLancamentosProps) {
  return (
    <Box
      titulo={titulo}
      contagem={lancamentos.length}
      destaque={destaque}
      acao={
        <Botao variante="primaria" tamanho="pequeno" onClick={acaoCriar.onClick}>
          {acaoCriar.rotulo}
        </Botao>
      }
    >
      {lancamentos.length === 0 ? (
        <div className="flex flex-col items-center justify-center px-5 py-12 text-center md:px-6">
          <p className="text-muted">{mensagemVazia}</p>
          <Botao variante="secundaria" tamanho="pequeno" onClick={acaoCriar.onClick} className="mt-4">
            {acaoCriar.rotulo}
          </Botao>
        </div>
      ) : (
        <ul className="divide-y divide-line">
          {lancamentos.map((lancamento) => (
            <li key={lancamento.id}>
              <LancamentoItem
                mesId={mesId}
                lancamento={lancamento}
                onEditar={() => onEditar(lancamento)}
                onRemover={() => onRemover(lancamento)}
              />
            </li>
          ))}
        </ul>
      )}
    </Box>
  )
}
