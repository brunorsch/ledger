import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Botao, CampoTexto, CampoValor } from '@/components'
import { ErroApi, type LancamentoResponse } from '@/api/types'
import { useAlterarLancamento, useCriarLancamento } from '@/api/queries'
import { centavosDoValor, valorDosCentavos } from '@/format/valor'

type FormularioLancamentoProps = {
  mesId: string
  tipo: 'DESPESA' | 'RECEITA'
  lancamento?: LancamentoResponse
  onSucesso: () => void
  onCancelar: () => void
}

export function FormularioLancamento({
  mesId,
  tipo,
  lancamento,
  onSucesso,
  onCancelar,
}: FormularioLancamentoProps) {
  const criar = useCriarLancamento(mesId)
  const alterar = useAlterarLancamento(mesId)

  const [descricao, setDescricao] = useState(lancamento?.descricao ?? '')
  const [centavos, setCentavos] = useState(lancamento ? centavosDoValor(lancamento.valor) : '')
  const [erros, setErros] = useState<{ descricao?: string; valor?: string }>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)

  const modo = lancamento ? 'editar' : 'criar'
  const enviando = criar.isPending || alterar.isPending

  function validar(): boolean {
    const proximosErros: { descricao?: string; valor?: string } = {}

    if (!descricao.trim()) {
      proximosErros.descricao = 'Informe uma descrição.'
    }

    if (!centavos) {
      proximosErros.valor = 'Informe um valor maior que zero.'
    }

    setErros(proximosErros)
    return Object.keys(proximosErros).length === 0
  }

  async function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setErroGeral(null)

    if (!validar()) return

    const valor = valorDosCentavos(centavos)

    try {
      if (modo === 'editar' && lancamento) {
        await alterar.mutateAsync({ id: lancamento.id, corpo: { descricao: descricao.trim(), valor } })
      } else {
        await criar.mutateAsync({ tipo, descricao: descricao.trim(), valor })
      }
      onSucesso()
    } catch (erro: unknown) {
      const mensagem = erro instanceof ErroApi ? erro.mensagem : 'Não foi possível salvar o lançamento.'
      setErroGeral(mensagem)
    }
  }

  function aoAlterarDescricao(evento: ChangeEvent<HTMLInputElement>) {
    setDescricao(evento.target.value)
  }

  return (
    <form onSubmit={aoSubmeter} className="space-y-5">
      <CampoTexto
        label="Descrição"
        value={descricao}
        onChange={aoAlterarDescricao}
        erro={erros.descricao}
        disabled={enviando}
        placeholder="Ex: Mercado"
      />

      <CampoValor
        label="Valor (R$)"
        centavos={centavos}
        onAlterar={setCentavos}
        erro={erros.valor}
        disabled={enviando}
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
