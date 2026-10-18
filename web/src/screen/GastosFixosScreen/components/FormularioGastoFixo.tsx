import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Botao, CampoTexto, CampoValor } from '@/components'
import { ErroApi, type GastoFixoResponse } from '@/api/types'
import { useAlterarGastoFixo, useCriarGastoFixo } from '@/api/queries'
import { normalizarValorMonetario } from '@/format/valor'

type FormularioGastoFixoProps = {
  gastoFixo?: GastoFixoResponse
  onSucesso: () => void
  onCancelar: () => void
}

export function FormularioGastoFixo({ gastoFixo, onSucesso, onCancelar }: FormularioGastoFixoProps) {
  const criar = useCriarGastoFixo()
  const alterar = useAlterarGastoFixo()

  const [descricao, setDescricao] = useState(gastoFixo?.descricao ?? '')
  const [valor, setValor] = useState(gastoFixo ? formatarValorParaInput(gastoFixo.valorPadrao) : '')
  const [erros, setErros] = useState<{ descricao?: string; valor?: string }>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)

  const modo = gastoFixo ? 'editar' : 'criar'
  const enviando = criar.isPending || alterar.isPending

  function validar(): boolean {
    const proximosErros: { descricao?: string; valor?: string } = {}

    if (!descricao.trim()) {
      proximosErros.descricao = 'Informe uma descrição.'
    }

    const valorNormalizado = normalizarValorMonetario(valor)
    if (!valorNormalizado) {
      proximosErros.valor = 'Informe um valor maior que zero, usando vírgula ou ponto.'
    }

    setErros(proximosErros)
    return Object.keys(proximosErros).length === 0
  }

  async function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setErroGeral(null)

    if (!validar()) return

    const valorNormalizado = normalizarValorMonetario(valor)!

    try {
      if (modo === 'editar' && gastoFixo) {
        await alterar.mutateAsync({
          id: gastoFixo.id,
          corpo: { descricao: descricao.trim(), valorPadrao: valorNormalizado },
        })
      } else {
        await criar.mutateAsync({ descricao: descricao.trim(), valorPadrao: valorNormalizado })
      }
      onSucesso()
    } catch (erro: unknown) {
      const mensagem = erro instanceof ErroApi ? erro.mensagem : 'Não foi possível salvar o gasto fixo.'
      setErroGeral(mensagem)
    }
  }

  function aoAlterarDescricao(evento: ChangeEvent<HTMLInputElement>) {
    setDescricao(evento.target.value)
  }

  function aoAlterarValor(evento: ChangeEvent<HTMLInputElement>) {
    setValor(evento.target.value)
  }

  return (
    <form onSubmit={aoSubmeter} className="space-y-5">
      <CampoTexto
        label="Descrição"
        value={descricao}
        onChange={aoAlterarDescricao}
        erro={erros.descricao}
        disabled={enviando}
        placeholder="Ex: Aluguel"
      />

      <CampoValor
        label="Valor padrão (R$)"
        value={valor}
        onChange={aoAlterarValor}
        erro={erros.valor}
        disabled={enviando}
        placeholder="0,00"
      />

      {erroGeral && (
        <p className="rounded-lg bg-danger-soft px-3 py-2 text-sm text-danger" role="alert">
          {erroGeral}
        </p>
      )}

      <div className="flex justify-end gap-3 pt-2">
        <Botao variante="secundaria" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </Botao>
        <Botao type="submit" variante="primaria" disabled={enviando}>
          {enviando ? 'Salvando...' : modo === 'editar' ? 'Salvar alterações' : 'Adicionar'}
        </Botao>
      </div>
    </form>
  )
}

function formatarValorParaInput(valor: string): string {
  return valor.replace('.', ',')
}
