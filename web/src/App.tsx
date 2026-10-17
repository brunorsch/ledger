import { useCallback, useEffect, useMemo, useState } from 'react'
import { ErroApi } from './api/tipos.ts'
import type { LancamentoResponse, MesResponse, MesSumarioResponse } from './api/tipos.ts'
import { listarLancamentos, obterMesPorReferencia, obterSumario } from './api/cliente.ts'
import { formatarBRL } from './formato/brl.ts'

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

type EstadoCarregamento =
  | { tipo: 'carregando' }
  | { tipo: 'erro'; mensagem: string }
  | {
      tipo: 'pronto'
      mes: MesResponse
      sumario: MesSumarioResponse
      lancamentos: LancamentoResponse[]
    }

function App() {
  const referencia = useMemo(() => resolverReferencia(), [])
  const [estado, setEstado] = useState<EstadoCarregamento>(() => ({ tipo: 'carregando' }))

  const carregar = useCallback(async () => {
    setEstado({ tipo: 'carregando' })

    try {
      const mes = await obterMesPorReferencia(referencia)
      const [sumario, lancamentos] = await Promise.all([
        obterSumario(mes.id),
        listarLancamentos(mes.id),
      ])

      setEstado({ tipo: 'pronto', mes, sumario, lancamentos })
    } catch (erro) {
      const mensagem =
        erro instanceof ErroApi && erro.status === 404
          ? 'Não foi possível carregar os dados do mês.'
          : 'Não foi possível carregar os dados do mês. Verifique sua conexão e tente novamente.'
      setEstado({ tipo: 'erro', mensagem })
    }
  }, [referencia])

  useEffect(() => {
    // Carrega os dados do mês na montagem do componente.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void carregar()
  }, [carregar])

  return (
    <div className="min-h-screen bg-fill px-4 py-6 text-ink md:px-8 md:py-10">
      <div className="mx-auto max-w-6xl">
        <header className="mb-8 md:mb-10">
          <p className="text-sm font-medium tracking-wide text-muted uppercase">Ledger</p>
          <h1 className="mt-1 font-display text-3xl font-semibold tracking-tight md:text-4xl">
            {formatarTituloMes(referencia)}
          </h1>
        </header>

        {estado.tipo === 'carregando' && <TelaCarregando />}
        {estado.tipo === 'erro' && <TelaErro mensagem={estado.mensagem} onTentarNovamente={carregar} />}
        {estado.tipo === 'pronto' && <TelaPronto {...estado} />}
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
      <button
        type="button"
        onClick={onTentarNovamente}
        className="mt-6 rounded-lg bg-ink px-5 py-2.5 text-sm font-medium text-paper transition hover:bg-accent focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
      >
        Tentar novamente
      </button>
    </div>
  )
}

function TelaPronto({
  mes,
  sumario,
  lancamentos,
}: {
  mes: MesResponse
  sumario: MesSumarioResponse
  lancamentos: LancamentoResponse[]
}) {
  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <section className="lg:col-span-2">
        <div className="rounded-2xl border border-line bg-paper shadow-sm">
          <div className="flex items-center justify-between border-b border-line px-5 py-4 md:px-6">
            <h2 className="font-display text-lg font-medium">Lançamentos</h2>
            <span className="rounded-full bg-fill px-2.5 py-1 text-xs font-medium text-muted">
              {lancamentos.length}
            </span>
          </div>

          {lancamentos.length === 0 ? (
            <div className="px-5 py-16 text-center md:px-6">
              <p className="text-muted">Nenhum lançamento neste mês.</p>
            </div>
          ) : (
            <ul className="divide-y divide-line">
              {lancamentos.map((lancamento) => (
                <li key={lancamento.id}>
                  <LancamentoItem lancamento={lancamento} />
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>

      <aside className="space-y-6">
        <BlocoSumario sumario={sumario} />
        <BlocoDistribuicao sumario={sumario} mes={mes} />
      </aside>
    </div>
  )
}

function LancamentoItem({ lancamento }: { lancamento: LancamentoResponse }) {
  const isDespesa = lancamento.tipo === 'DESPESA'
  const isReceita = lancamento.tipo === 'RECEITA'

  return (
    <div className="flex items-center justify-between gap-4 px-5 py-4 md:px-6">
      <div className="min-w-0">
        <div className="flex items-center gap-2">
          <span
            className={`inline-flex h-2 w-2 rounded-full ${
              isReceita ? 'bg-success' : 'bg-danger'
            }`}
            aria-hidden="true"
          />
          <p className="truncate font-medium">{lancamento.descricao}</p>
        </div>
        <p className="mt-1 text-xs text-subtle">
          {isDespesa ? (
            lancamento.pago ? (
              <span className="inline-flex items-center gap-1 text-success">
                <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                Pago
              </span>
            ) : (
              <span className="text-warning">Não pago</span>
            )
          ) : (
            <span>Receita</span>
          )}
        </p>
      </div>
      <p
        className={`shrink-0 font-medium tabular-nums ${
          isReceita ? 'text-success' : 'text-ink'
        }`}
      >
        {isDespesa ? '-' : '+'}
        {formatarBRL(lancamento.valor)}
      </p>
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
    <div className="rounded-2xl border border-line bg-paper shadow-sm">
      <div className="border-b border-line px-5 py-4 md:px-6">
        <h2 className="font-display text-lg font-medium">Sumário</h2>
      </div>
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
    </div>
  )
}

function BlocoDistribuicao({ sumario, mes }: { sumario: MesSumarioResponse; mes: MesResponse }) {
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

  return (
    <div className="rounded-2xl border border-line bg-paper shadow-sm">
      <div className="border-b border-line px-5 py-4 md:px-6">
        <h2 className="font-display text-lg font-medium">Distribuição das sobras</h2>
        {!restanteNegativo && (
          <p className="mt-1 text-xs text-muted">
            Baseado nos parâmetros de {formatarMesCurto(mes.mesReferencia)}.
          </p>
        )}
      </div>
      <dl className="divide-y divide-line">
        {itens.map(({ rotulo, valor, destaque }) => (
          <div key={rotulo} className="flex items-center justify-between px-5 py-3.5 md:px-6">
            <dt className="text-sm text-muted">{rotulo}</dt>
            <dd
              className={`font-medium tabular-nums ${
                destaque ? 'text-danger' : 'text-ink'
              }`}
            >
              {formatarBRL(valor)}
            </dd>
          </div>
        ))}
      </dl>
    </div>
  )
}

export default App
