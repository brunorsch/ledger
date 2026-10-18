import { useMemo, useState } from 'react'
import { ErroApi, type LancamentoResponse, type MesResponse, type MesSumarioResponse } from './api/tipos.ts'
import {
  useAlterarAlternancias,
  useAlternarPagoLancamento,
  useLancamentos,
  useMes,
  useSumario,
} from './api/consultas.ts'
import { Box } from './Box.tsx'
import { Botao } from './componentes/Botao.tsx'
import { Interruptor } from './componentes/Interruptor.tsx'
import { Modal } from './componentes/Modal.tsx'
import { formatarBRL } from './formato/brl.ts'
import { ConfirmacaoRemocao } from './lancamentos/ConfirmacaoRemocao.tsx'
import { FormularioLancamento } from './lancamentos/FormularioLancamento.tsx'

const MES_ATUAL = (() => {
  const hoje = new Date()
  const ano = hoje.getFullYear()
  const mes = String(hoje.getMonth() + 1).padStart(2, '0')
  return `${ano}-${mes}`
})()

const REFERENCIA_REGEX = /^\d{4}-\d{2}$/

function resolverReferencia(): string {
  const params = new URLSearchParams(window.location.search)
  const referencia = params.get('referencia')
  return referencia && REFERENCIA_REGEX.test(referencia) ? referencia : MES_ATUAL
}

function formatarTituloMes(referencia: string): string {
  const [ano, mes] = referencia.split('-').map(Number)
  const data = new Date(ano, mes - 1, 1)
  return data.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
}

function formatarMesCurto(referencia: string): string {
  const [ano, mes] = referencia.split('-').map(Number)
  const data = new Date(ano, mes - 1, 1)
  return data.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')
}

type ModalAtivo =
  | { tipo: 'criar'; tipoLancamento: 'DESPESA' | 'RECEITA' }
  | { tipo: 'editar'; lancamento: LancamentoResponse }
  | { tipo: 'remover'; lancamento: LancamentoResponse }
  | null

function App() {
  const referencia = useMemo(() => resolverReferencia(), [])

  const { data: mes, isLoading: carregandoMes, error: erroMes, refetch: recarregarMes } = useMes(referencia)
  const mesId = mes?.id ?? ''

  const {
    data: sumario,
    isLoading: carregandoSumario,
    error: erroSumario,
    refetch: recarregarSumario,
  } = useSumario(mesId)

  const {
    data: lancamentos,
    isLoading: carregandoLancamentos,
    error: erroLancamentos,
    refetch: recarregarLancamentos,
  } = useLancamentos(mesId)

  const [modalAtivo, setModalAtivo] = useState<ModalAtivo>(null)

  const carregando = carregandoMes || carregandoSumario || carregandoLancamentos
  const erro = erroMes || erroSumario || erroLancamentos

  if (carregando) {
    return (
      <div className="min-h-screen bg-fill px-4 py-6 text-ink md:px-8 md:py-10">
        <div className="mx-auto max-w-6xl">
          <TelaCarregando />
        </div>
      </div>
    )
  }

  if (erro || !mes || !sumario || !lancamentos) {
    return (
      <div className="min-h-screen bg-fill px-4 py-6 text-ink md:px-8 md:py-10">
        <div className="mx-auto max-w-6xl">
          <TelaErro
            mensagem="Não foi possível carregar os dados do mês. Verifique sua conexão e tente novamente."
            onTentarNovamente={() => {
              void recarregarMes()
              void recarregarSumario()
              void recarregarLancamentos()
            }}
          />
        </div>
      </div>
    )
  }

  function fecharModal() {
    setModalAtivo(null)
  }

  return (
    <div className="min-h-screen bg-fill px-4 py-6 text-ink md:px-8 md:py-10">
      <div className="mx-auto max-w-6xl">
        <header className="mb-8 md:mb-10">
          <p className="text-sm font-medium tracking-wide text-muted uppercase">Ledger</p>
          <h1 className="mt-1 font-display text-3xl font-semibold tracking-tight md:text-4xl">
            {formatarTituloMes(referencia)}
          </h1>
        </header>

        <TelaPronto
          mes={mes}
          sumario={sumario}
          lancamentos={lancamentos}
          onCriar={(tipo) => setModalAtivo({ tipo: 'criar', tipoLancamento: tipo })}
          onEditar={(lancamento) => setModalAtivo({ tipo: 'editar', lancamento })}
          onRemover={(lancamento) => setModalAtivo({ tipo: 'remover', lancamento })}
        />

        <Modal
          aberto={modalAtivo?.tipo === 'criar'}
          titulo={modalAtivo?.tipo === 'criar' ? (modalAtivo.tipoLancamento === 'DESPESA' ? 'Nova despesa' : 'Nova receita') : ''}
          onFechar={fecharModal}
        >
          {modalAtivo?.tipo === 'criar' && (
            <FormularioLancamento
              mesId={mes.id}
              tipo={modalAtivo.tipoLancamento}
              onSucesso={fecharModal}
              onCancelar={fecharModal}
            />
          )}
        </Modal>

        <Modal
          aberto={modalAtivo?.tipo === 'editar'}
          titulo="Editar lançamento"
          onFechar={fecharModal}
        >
          {modalAtivo?.tipo === 'editar' && (
            <FormularioLancamento
              mesId={mes.id}
              tipo={modalAtivo.lancamento.tipo}
              lancamento={modalAtivo.lancamento}
              onSucesso={fecharModal}
              onCancelar={fecharModal}
            />
          )}
        </Modal>

        <Modal
          aberto={modalAtivo?.tipo === 'remover'}
          titulo="Remover lançamento"
          onFechar={fecharModal}
        >
          {modalAtivo?.tipo === 'remover' && (
            <ConfirmacaoRemocao
              mesId={mes.id}
              lancamento={modalAtivo.lancamento}
              onSucesso={fecharModal}
              onCancelar={fecharModal}
            />
          )}
        </Modal>
      </div>
    </div>
  )
}

function TelaCarregando() {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-line bg-paper py-24 shadow-sm">
      <div className="h-10 w-10 animate-spin rounded-full border-4 border-line border-t-accent" />
      <p className="mt-4 text-muted">Carregando mês...</p>
    </div>
  )
}

function TelaErro({ mensagem, onTentarNovamente }: { mensagem: string; onTentarNovamente: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-line bg-paper px-6 py-20 text-center shadow-sm">
      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-danger-soft text-danger">
        <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h2 className="mt-4 font-display text-lg font-medium">Algo deu errado</h2>
      <p className="mt-2 max-w-sm text-muted">{mensagem}</p>
      <Botao variante="primaria" onClick={onTentarNovamente} className="mt-6">
        Tentar novamente
      </Botao>
    </div>
  )
}

function TelaPronto({
  mes,
  sumario,
  lancamentos,
  onCriar,
  onEditar,
  onRemover,
}: {
  mes: MesResponse
  sumario: MesSumarioResponse
  lancamentos: LancamentoResponse[]
  onCriar: (tipo: 'DESPESA' | 'RECEITA') => void
  onEditar: (lancamento: LancamentoResponse) => void
  onRemover: (lancamento: LancamentoResponse) => void
}) {
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

function CardLancamentos({
  titulo,
  lancamentos,
  destaque = false,
  mensagemVazia,
  acaoCriar,
  mesId,
  onEditar,
  onRemover,
}: {
  titulo: string
  lancamentos: LancamentoResponse[]
  destaque?: boolean
  mensagemVazia: string
  acaoCriar: { rotulo: string; onClick: () => void }
  mesId: string
  onEditar: (lancamento: LancamentoResponse) => void
  onRemover: (lancamento: LancamentoResponse) => void
}) {
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

function LancamentoItem({
  mesId,
  lancamento,
  onEditar,
  onRemover,
}: {
  mesId: string
  lancamento: LancamentoResponse
  onEditar: () => void
  onRemover: () => void
}) {
  const isDespesa = lancamento.tipo === 'DESPESA'
  const isReceita = lancamento.tipo === 'RECEITA'
  const alternarPago = useAlternarPagoLancamento(mesId)

  async function aoAlternarPago() {
    if (!isDespesa) return
    try {
      await alternarPago.mutateAsync({ id: lancamento.id, pago: !lancamento.pago })
    } catch {
      // Em caso de erro nada é invalidado: a listagem mantém o status anterior.
    }
  }

  return (
    <div className="flex items-center justify-between gap-3 px-5 py-4 md:px-6">
      <div className="min-w-0">
        <div className="flex items-center gap-2">
          <span
            className={`inline-flex h-2 w-2 rounded-full ${isReceita ? 'bg-success' : 'bg-danger'}`}
            aria-hidden="true"
          />
          <p className="truncate font-medium">{lancamento.descricao}</p>
        </div>
        <p className="mt-1 text-xs text-subtle">
          {isDespesa ? (
            <button
              type="button"
              onClick={aoAlternarPago}
              disabled={alternarPago.isPending}
              aria-pressed={lancamento.pago}
              className={`inline-flex items-center gap-1 transition hover:underline disabled:cursor-not-allowed disabled:opacity-60 ${
                lancamento.pago ? 'text-success' : 'text-warning'
              }`}
            >
              {lancamento.pago ? (
                <>
                  <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  Pago
                </>
              ) : (
                <>Não pago</>
              )}
            </button>
          ) : (
            <span>Receita</span>
          )}
        </p>
      </div>

      <div className="flex shrink-0 items-center gap-3">
        <p
          className={`font-medium tabular-nums ${isReceita ? 'text-success' : 'text-ink'}`}
        >
          {isDespesa ? '-' : '+'}
          {formatarBRL(lancamento.valor)}
        </p>

        <div className="flex items-center gap-1">
          <Botao
            variante="fantasma"
            tamanho="pequeno"
            onClick={onEditar}
            aria-label={`Editar ${lancamento.descricao}`}
            className="h-8 w-8 p-0"
          >
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
            </svg>
          </Botao>
          <Botao
            variante="fantasma"
            tamanho="pequeno"
            onClick={onRemover}
            aria-label={`Remover ${lancamento.descricao}`}
            className="h-8 w-8 p-0 text-danger hover:bg-danger-soft hover:text-danger"
          >
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </Botao>
        </div>
      </div>
    </div>
  )
}

function BlocoSumario({ sumario }: { sumario: MesSumarioResponse }) {
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

function BlocoDistribuicao({ sumario, mesReferencia }: { sumario: MesSumarioResponse; mesReferencia: string }) {
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

function BlocoConfiguracoes({ mes }: { mes: MesResponse }) {
  const alternar = useAlterarAlternancias(mes.id)
  const [erro, setErro] = useState<string | null>(null)

  const alternarInvestir = Boolean(mes.alternarInvestir)
  const alternarLazer = Boolean(mes.alternarLazer)

  async function aoAlternarInvestir(ligado: boolean) {
    setErro(null)

    try {
      await alternar.mutateAsync({ alternarInvestir: ligado })
    } catch (err) {
      setErro(mensagemDeErroAoAlternar(err))
    }
  }

  async function aoAlternarLazer(ligado: boolean) {
    setErro(null)

    try {
      await alternar.mutateAsync({ alternarLazer: ligado })
    } catch (err) {
      setErro(mensagemDeErroAoAlternar(err))
    }
  }

  return (
    <Box titulo="Configurações">
      <div className="space-y-4 px-5 py-4 md:px-6">
        <Interruptor
          rotulo="Investir"
          descricao="Reservar parte do restante para investimentos."
          checked={alternarInvestir}
          disabled={alternar.isPending}
          onChange={aoAlternarInvestir}
        />
        <Interruptor
          rotulo="Lazer"
          descricao="Separar parcela do restante para lazer."
          checked={alternarLazer}
          disabled={alternar.isPending}
          onChange={aoAlternarLazer}
        />
        {erro && (
          <p className="text-sm text-danger" role="alert">
            {erro}
          </p>
        )}
      </div>
    </Box>
  )
}

function mensagemDeErroAoAlternar(err: unknown): string {
  if (err instanceof ErroApi) return err.mensagem
  return 'Não foi possível salvar. Tente novamente.'
}

export default App
