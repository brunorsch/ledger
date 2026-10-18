import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Botao, CampoTexto, CampoValor } from '@/components'
import { ErroApi } from '@/api/types'
import { useCriarMes } from '@/api/queries'

const VERSAO_ESQUEMA_PADRAO = 1

type ValoresIniciais = {
  essenciais: string
  lazer: string
  tetoEssenciais: string
  tetoLazer: string
}

type ErrosFormulario = {
  essenciais?: string
  lazer?: string
  tetoEssenciais?: string
  tetoLazer?: string
}

type FormularioCriarMesProps = {
  referencia: string
  valoresIniciais: ValoresIniciais
  usandoPadroes: boolean
  carregandoUltimoMes: boolean
  alternarInvestir: boolean
  alternarLazer: boolean
  onSucesso: () => void
  onCancelar: () => void
}

export function FormularioCriarMes({
  referencia,
  valoresIniciais,
  usandoPadroes,
  carregandoUltimoMes,
  alternarInvestir,
  alternarLazer,
  onSucesso,
  onCancelar,
}: FormularioCriarMesProps) {
  const criar = useCriarMes(referencia)

  const [essenciais, setEssenciais] = useState(valoresIniciais.essenciais)
  const [lazer, setLazer] = useState(valoresIniciais.lazer)
  const [tetoEssenciais, setTetoEssenciais] = useState(valoresIniciais.tetoEssenciais)
  const [tetoLazer, setTetoLazer] = useState(valoresIniciais.tetoLazer)
  const [erros, setErros] = useState<ErrosFormulario>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)

  if (carregandoUltimoMes) {
    return (
      <div className="flex flex-col items-center justify-center py-10 text-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-line border-t-accent" />
        <p className="mt-3 text-sm text-muted">Buscando parâmetros do último mês...</p>
      </div>
    )
  }

  function validar(): boolean {
    const proximosErros: ErrosFormulario = {}

    const percentualEssenciais = parsearPercentual(essenciais)
    const percentualLazer = parsearPercentual(lazer)

    if (percentualEssenciais === null) {
      proximosErros.essenciais = 'Informe um percentual entre 0 e 100.'
    }

    if (percentualLazer === null) {
      proximosErros.lazer = 'Informe um percentual entre 0 e 100.'
    }

    if (percentualEssenciais !== null && percentualLazer !== null) {
      if (percentualEssenciais + percentualLazer > 100) {
        proximosErros.lazer = 'A soma dos percentuais não pode passar de 100%.'
      }
    }

    const tetoEssenciaisNormalizado = normalizarValorMonetarioZero(tetoEssenciais)
    if (tetoEssenciaisNormalizado === null) {
      proximosErros.tetoEssenciais = 'Informe um valor válido.'
    }

    const tetoLazerNormalizado = normalizarValorMonetarioZero(tetoLazer)
    if (tetoLazerNormalizado === null) {
      proximosErros.tetoLazer = 'Informe um valor válido.'
    }

    setErros(proximosErros)
    return Object.keys(proximosErros).length === 0
  }

  async function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setErroGeral(null)

    if (!validar()) return

    const percentualEssenciais = parsearPercentual(essenciais)!
    const percentualLazer = parsearPercentual(lazer)!

    try {
      await criar.mutateAsync({
        mesReferencia: `${referencia}-01`,
        parametrosCalculo: {
          versaoEsquema: VERSAO_ESQUEMA_PADRAO,
          percentuais: {
            essenciais: percentualEssenciais / 100,
            lazer: percentualLazer / 100,
          },
          tetos: {
            essenciais: normalizarValorMonetarioZero(tetoEssenciais)!,
            lazer: normalizarValorMonetarioZero(tetoLazer)!,
          },
        },
        alternarInvestir,
        alternarLazer,
      })
      onSucesso()
    } catch (erro: unknown) {
      const mensagem = erro instanceof ErroApi ? erro.mensagem : 'Não foi possível criar o mês.'
      setErroGeral(mensagem)
    }
  }

  function aoAlterarEssenciais(evento: ChangeEvent<HTMLInputElement>) {
    setEssenciais(evento.target.value)
  }

  function aoAlterarLazer(evento: ChangeEvent<HTMLInputElement>) {
    setLazer(evento.target.value)
  }

  function aoAlterarTetoEssenciais(evento: ChangeEvent<HTMLInputElement>) {
    setTetoEssenciais(evento.target.value)
  }

  function aoAlterarTetoLazer(evento: ChangeEvent<HTMLInputElement>) {
    setTetoLazer(evento.target.value)
  }

  return (
    <form onSubmit={aoSubmeter} className="space-y-5">
      {usandoPadroes && (
        <p className="text-sm text-muted">Parâmetros iniciais sugeridos</p>
      )}

      <div className="grid grid-cols-2 gap-4">
        <CampoTexto
          label="Essenciais (%)"
          inputMode="decimal"
          value={essenciais}
          onChange={aoAlterarEssenciais}
          erro={erros.essenciais}
          disabled={criar.isPending}
          placeholder="0,0"
        />

        <CampoTexto
          label="Lazer (%)"
          inputMode="decimal"
          value={lazer}
          onChange={aoAlterarLazer}
          erro={erros.lazer}
          disabled={criar.isPending}
          placeholder="0,0"
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <CampoValor
          label="Teto essenciais (R$)"
          value={tetoEssenciais}
          onChange={aoAlterarTetoEssenciais}
          erro={erros.tetoEssenciais}
          disabled={criar.isPending}
          placeholder="0,00"
        />

        <CampoValor
          label="Teto lazer (R$)"
          value={tetoLazer}
          onChange={aoAlterarTetoLazer}
          erro={erros.tetoLazer}
          disabled={criar.isPending}
          placeholder="0,00"
        />
      </div>

      {erroGeral && (
        <p className="rounded-lg bg-danger-soft px-3 py-2 text-sm text-danger" role="alert">
          {erroGeral}
        </p>
      )}

      <div className="flex justify-end gap-3 pt-2">
        <Botao variante="secundaria" onClick={onCancelar} disabled={criar.isPending}>
          Cancelar
        </Botao>
        <Botao type="submit" variante="primaria" disabled={criar.isPending}>
          {criar.isPending ? 'Criando...' : 'Criar mês'}
        </Botao>
      </div>
    </form>
  )
}

function parsearPercentual(valor: string): number | null {
  const limpo = valor.replace(',', '.')
  if (!/^\d+(\.\d{1})?$/.test(limpo)) return null
  const numero = Number(limpo)
  if (Number.isNaN(numero) || numero < 0 || numero > 100) return null
  return numero
}

function normalizarValorMonetarioZero(valor: string): string | null {
  const limpo = valor.trim().replace(/\./g, '').replace(',', '.')

  if (!/^\d+(\.\d{1,2})?$/.test(limpo)) return null

  const [inteiro, decimal = ''] = limpo.split('.')
  const decimalAjustado = decimal.padEnd(2, '0')

  return `${inteiro}.${decimalAjustado}`
}
