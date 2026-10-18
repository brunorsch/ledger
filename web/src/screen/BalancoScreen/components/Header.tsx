import { useNavigate } from 'react-router-dom'
import { formatarTituloMes, mesAtual, somarMeses } from '@/format/mes'
import { Botao } from '@/components'
import { useAtalhoMesAtual } from './useAtalhoMesAtual'

type HeaderProps = {
  referencia: string
}

export function Header({ referencia }: HeaderProps) {
  const navigate = useNavigate()

  function navegarPara(referenciaDestino: string) {
    navigate(`/meses/${referenciaDestino}`)
  }

  function irParaMesAtual() {
    navegarPara(mesAtual())
  }

  useAtalhoMesAtual(navegarPara)

  function aoMudarSeletor(evento: React.ChangeEvent<HTMLInputElement>) {
    const valor = evento.target.value
    if (/^\d{4}-\d{2}$/.test(valor)) {
      navegarPara(valor)
    }
  }

  return (
    <header className="mb-8 md:mb-10">
      <div className="rounded-2xl border border-line bg-paper px-4 py-5 md:px-6 md:py-6">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="font-display text-3xl font-semibold tracking-tight text-ink md:text-4xl">
              Balanço
            </h1>
            <p className="mt-1 text-sm font-medium text-muted">
              {formatarTituloMes(referencia)} · {referencia}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2 md:gap-3">
            <div className="flex items-center gap-1">
              <Botao
                variante="fantasma"
                tamanho="pequeno"
                onClick={() => navegarPara(somarMeses(referencia, -1))}
                aria-label="Mês anterior"
                title="Mês anterior"
              >
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </Botao>

              <input
                type="month"
                value={referencia}
                onChange={aoMudarSeletor}
                className="h-9 rounded-lg border border-line bg-fill px-2.5 text-sm font-medium text-ink outline-none focus:border-accent focus:ring-2 focus:ring-accent/10"
                aria-label="Selecionar mês"
              />

              <Botao
                variante="fantasma"
                tamanho="pequeno"
                onClick={() => navegarPara(somarMeses(referencia, 1))}
                aria-label="Próximo mês"
                title="Próximo mês"
              >
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Botao>
            </div>

            <Botao variante="secundaria" tamanho="pequeno" onClick={irParaMesAtual}>
              Ir para Mês Atual
            </Botao>
          </div>
        </div>
      </div>
    </header>
  )
}
