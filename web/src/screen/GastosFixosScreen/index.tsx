import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Botao, Box, Modal } from '@/components'
import { useGastosFixos } from '@/api/queries'
import { useAutenticacao } from '@/auth/useAutenticacao'
import { GastoFixoItem } from './components/GastoFixoItem'
import { FormularioGastoFixo } from './components/FormularioGastoFixo'
import { ConfirmacaoRemocaoGastoFixo } from './components/ConfirmacaoRemocaoGastoFixo'
import { TelaCarregando } from './components/TelaCarregando'
import { TelaErro } from './components/TelaErro'
import type { ModalAtivoGastoFixo } from './components/types'

export default function GastosFixosScreen() {
  const navigate = useNavigate()
  const sair = useAutenticacao((estado) => estado.sair)
  const { data: gastosFixos, isLoading, error, refetch } = useGastosFixos()
  const [modalAtivo, setModalAtivo] = useState<ModalAtivoGastoFixo>(null)

  useEffect(() => {
    document.title = 'Ledger · Gastos fixos'
  }, [])

  async function aoSair() {
    await sair()
    navigate('/login', { replace: true })
  }

  function fecharModal() {
    setModalAtivo(null)
  }

  function abrirCriar() {
    setModalAtivo({ tipo: 'criar' })
  }

  const gastosAtivos = gastosFixos?.filter((g) => g.ativo) ?? []

  return (
    <div>
      <header className="mb-8 md:mb-10">
        <div className="rounded-2xl border border-line bg-paper px-4 py-5 md:px-6 md:py-6">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <h1 className="font-display text-3xl font-semibold tracking-tight text-ink md:text-4xl">
                Gastos fixos
              </h1>
              <p className="mt-1 text-sm text-muted">
                Despesas recorrentes que aparecem automaticamente em cada mês.
              </p>
            </div>
            <div className="flex flex-wrap items-center gap-2 md:gap-3">
              <Botao variante="primaria" onClick={abrirCriar}>
                Adicionar gasto fixo
              </Botao>

              <Botao variante="fantasma" tamanho="pequeno" onClick={() => void aoSair()}>
                Sair
              </Botao>
            </div>
          </div>
        </div>
      </header>

      {isLoading ? (
        <TelaCarregando />
      ) : error ? (
        <TelaErro
          mensagem="Não foi possível carregar os gastos fixos. Verifique sua conexão e tente novamente."
          onTentarNovamente={() => refetch()}
        />
      ) : (
        <Box
          titulo="Gastos fixos ativos"
          contagem={gastosAtivos.length}
          acao={
            <Botao variante="primaria" tamanho="pequeno" onClick={abrirCriar}>
              Adicionar
            </Botao>
          }
        >
          {gastosAtivos.length === 0 ? (
            <div className="flex flex-col items-center justify-center px-5 py-12 text-center md:px-6">
              <p className="text-muted">Nenhum gasto fixo cadastrado.</p>
              <Botao variante="secundaria" tamanho="pequeno" onClick={abrirCriar} className="mt-4">
                Adicionar gasto fixo
              </Botao>
            </div>
          ) : (
            <ul className="divide-y divide-line">
              {gastosAtivos.map((gastoFixo) => (
                <li key={gastoFixo.id}>
                  <GastoFixoItem
                    gastoFixo={gastoFixo}
                    onEditar={(g) => setModalAtivo({ tipo: 'editar', gastoFixo: g })}
                    onRemover={(g) => setModalAtivo({ tipo: 'remover', gastoFixo: g })}
                  />
                </li>
              ))}
            </ul>
          )}
        </Box>
      )}

      <Modal
        aberto={modalAtivo?.tipo === 'criar'}
        titulo="Novo gasto fixo"
        onFechar={fecharModal}
      >
        {modalAtivo?.tipo === 'criar' && (
          <FormularioGastoFixo onSucesso={fecharModal} onCancelar={fecharModal} />
        )}
      </Modal>

      <Modal
        aberto={modalAtivo?.tipo === 'editar'}
        titulo="Editar gasto fixo"
        onFechar={fecharModal}
      >
        {modalAtivo?.tipo === 'editar' && (
          <FormularioGastoFixo
            gastoFixo={modalAtivo.gastoFixo}
            onSucesso={fecharModal}
            onCancelar={fecharModal}
          />
        )}
      </Modal>

      <Modal
        aberto={modalAtivo?.tipo === 'remover'}
        titulo="Remover gasto fixo"
        onFechar={fecharModal}
      >
        {modalAtivo?.tipo === 'remover' && (
          <ConfirmacaoRemocaoGastoFixo
            gastoFixo={modalAtivo.gastoFixo}
            onSucesso={fecharModal}
            onCancelar={fecharModal}
          />
        )}
      </Modal>
    </div>
  )
}
