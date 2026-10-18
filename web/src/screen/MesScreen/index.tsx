import { useMemo, useState } from "react"
import { useLancamentos, useMes, useSumario } from "@/api/queries"
import { Header } from "./components/Header"
import { TelaCarregando } from "./components/TelaCarregando"
import { TelaErro } from "./components/TelaErro"
import { TelaPronto } from "./components/TelaPronto"
import { ModaisMes } from "./components/ModaisMes"
import type { ModalAtivo } from "./components/types"

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

function MesScreen() {
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

  function fecharModal() {
    setModalAtivo(null)
  }

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

  return (
    <div className="min-h-screen bg-fill px-4 py-6 text-ink md:px-8 md:py-10">
      <div className="mx-auto max-w-6xl">
        <Header referencia={referencia} />

        <TelaPronto
          mes={mes}
          sumario={sumario}
          lancamentos={lancamentos}
          onCriar={(tipo) => setModalAtivo({ tipo: 'criar', tipoLancamento: tipo })}
          onEditar={(lancamento) => setModalAtivo({ tipo: 'editar', lancamento })}
          onRemover={(lancamento) => setModalAtivo({ tipo: 'remover', lancamento })}
        />

        <ModaisMes modalAtivo={modalAtivo} mesId={mes.id} onFechar={fecharModal} />
      </div>
    </div>
  )
}

export default MesScreen
