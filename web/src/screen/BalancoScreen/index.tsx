import { useParams, useLoaderData } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useLancamentos, useMes, useSumario } from '@/api/queries'
import { Header } from './components/Header'
import { TelaCarregando } from './components/TelaCarregando'
import { TelaErro } from './components/TelaErro'
import { TelaPronto } from './components/TelaPronto'
import { ModaisMes } from './components/ModaisMes'
import type { ModalAtivo } from './components/types'

export default function BalancoScreen() {
  const { referencia } = useParams<{ referencia: string }>()
  const loaderData = useLoaderData() as { referencia: string } | undefined
  const referenciaValida = loaderData?.referencia ?? referencia ?? ''

  useEffect(() => {
    document.title = `Ledger · Balanço · ${referenciaValida}`
  }, [referenciaValida])

  const { data: mes, isLoading: carregandoMes, error: erroMes, refetch: recarregarMes } = useMes(referenciaValida)
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
      <div className="min-h-[calc(100svh-7rem)] md:min-h-[calc(100svh-5rem)]">
        <Header referencia={referenciaValida} />
        <TelaCarregando />
      </div>
    )
  }

  if (erro || !mes || !sumario || !lancamentos) {
    return (
      <div className="min-h-[calc(100svh-7rem)] md:min-h-[calc(100svh-5rem)]">
        <Header referencia={referenciaValida} />
        <TelaErro
          mensagem="Não foi possível carregar os dados do mês. Verifique sua conexão e tente novamente."
          onTentarNovamente={() => {
            void recarregarMes()
            void recarregarSumario()
            void recarregarLancamentos()
          }}
        />
      </div>
    )
  }

  return (
    <div>
      <Header referencia={referenciaValida} />

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
  )
}
