import { useParams, useLoaderData } from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import { ErroApi } from '@/api/types'
import { useLancamentos, useMes, useSumario, useUltimoMes } from '@/api/queries'
import { Botao, Modal } from '@/components'
import { Header } from './components/Header'
import { TelaCarregando } from './components/TelaCarregando'
import { TelaErro } from './components/TelaErro'
import { TelaPronto } from './components/TelaPronto'
import { FormularioCriarMes } from './components/FormularioCriarMes'
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
  const [modalCriarMesAberto, setModalCriarMesAberto] = useState(false)

  const { data: ultimoMes, isLoading: carregandoUltimoMes } = useUltimoMes(modalCriarMesAberto)

  const carregando = carregandoMes || carregandoSumario || carregandoLancamentos
  const erro = erroMes || erroSumario || erroLancamentos
  const mesNaoExiste = erroMes instanceof ErroApi && erroMes.status === 404

  const valoresIniciaisFormulario = useMemo(() => {
    if (ultimoMes) {
      return {
        essenciais: formatarPercentual(ultimoMes.parametrosCalculo.percentuais.essenciais),
        lazer: formatarPercentual(ultimoMes.parametrosCalculo.percentuais.lazer),
        tetoEssenciais: formatarValorParaInput(ultimoMes.parametrosCalculo.tetos.essenciais),
        tetoLazer: formatarValorParaInput(ultimoMes.parametrosCalculo.tetos.lazer),
      }
    }

    return {
      essenciais: '50.0',
      lazer: '30.0',
      tetoEssenciais: '0,00',
      tetoLazer: '0,00',
    }
  }, [ultimoMes])

  const usandoPadroes = !ultimoMes && !carregandoUltimoMes
  const alternarInvestir = ultimoMes?.alternarInvestir ?? true
  const alternarLazer = ultimoMes?.alternarLazer ?? true

  function fecharModal() {
    setModalAtivo(null)
  }

  function fecharModalCriarMes() {
    setModalCriarMesAberto(false)
  }

  function aoCriarMesComSucesso() {
    fecharModalCriarMes()
    void recarregarMes()
  }

  if (carregando) {
    return (
      <div className="min-h-[calc(100svh-7rem)] md:min-h-[calc(100svh-5rem)]">
        <Header referencia={referenciaValida} />
        <TelaCarregando />
      </div>
    )
  }

  if (mesNaoExiste) {
    return (
      <div className="min-h-[calc(100svh-7rem)] md:min-h-[calc(100svh-5rem)]">
        <Header referencia={referenciaValida} />
        <div className="rounded-2xl border border-line bg-paper px-6 py-16 text-center shadow-sm md:py-20">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-fill text-muted mx-auto">
            <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
          <h2 className="mt-4 font-display text-lg font-medium">Este mês ainda não foi criado</h2>
          <p className="mt-2 text-muted">{referenciaValida}</p>
          <Botao variante="primaria" onClick={() => setModalCriarMesAberto(true)} className="mt-6">
            Criar mês
          </Botao>
        </div>

        <Modal
          aberto={modalCriarMesAberto}
          titulo="Criar mês"
          onFechar={fecharModalCriarMes}
        >
          <FormularioCriarMes
            key={`${modalCriarMesAberto ? 'aberto' : 'fechado'}-${ultimoMes?.id ?? 'padrao'}`}
            referencia={referenciaValida}
            valoresIniciais={valoresIniciaisFormulario}
            usandoPadroes={usandoPadroes}
            carregandoUltimoMes={carregandoUltimoMes}
            alternarInvestir={alternarInvestir}
            alternarLazer={alternarLazer}
            onSucesso={aoCriarMesComSucesso}
            onCancelar={fecharModalCriarMes}
          />
        </Modal>
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

function formatarPercentual(fração: number): string {
  return (fração * 100).toFixed(1)
}

function formatarValorParaInput(valor: string): string {
  return valor.replace('.', ',')
}
