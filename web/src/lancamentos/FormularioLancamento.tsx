import { useState } from 'react'
import { Botao } from '../componentes/Botao.tsx'
import { CampoTexto } from '../componentes/CampoTexto.tsx'
import { CampoValor } from '../componentes/CampoValor.tsx'
import { ErroApi, type LancamentoResponse } from '../api/tipos.ts'
import { useAlterarLancamento, useCriarLancamento } from '../api/consultas.ts'
import { normalizarValorMonetario } from '../formato/valor.ts'

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
  const [valor, setValor] = useState(lancamento ? formatarValorParaInput(lancamento.valor) : '')
  const [erros, setErros] = useState<{ descricao?: string; valor?: string }>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)

  const modo = lancamento ? 'editar' : 'criar'
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

  async function aoSubmeter(evento: React.FormEvent) {
    evento.preventDefault()
    setErroGeral(null)

    if (!validar()) return

    const valorNormalizado = normalizarValorMonetario(valor)!

    try {
      if (modo === 'editar' && lancamento) {
        await alterar.mutateAsync({ id: lancamento.id, corpo: { descricao: descricao.trim(), valor: valorNormalizado } })
      } else {
        await criar.mutateAsync({ tipo, descricao: descricao.trim(), valor: valorNormalizado })
      }
      onSucesso()
    } catch (erro) {
      const mensagem = erro instanceof ErroApi ? erro.mensagem : 'Não foi possível salvar o lançamento.'
      setErroGeral(mensagem)
    }
  }

  return (
    <form onSubmit={aoSubmeter} className="space-y-5">
      <CampoTexto
        label="Descrição"
        value={descricao}
        onChange={(evento) => setDescricao(evento.target.value)}
        erro={erros.descricao}
        disabled={enviando}
        placeholder="Ex: Mercado"
      />

      <CampoValor
        label="Valor (R$)"
        value={valor}
        onChange={(evento) => setValor(evento.target.value)}
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
