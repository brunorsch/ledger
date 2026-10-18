import { Modal } from '@/components'
import { FormularioLancamento } from './FormularioLancamento'
import { ConfirmacaoRemocao } from './ConfirmacaoRemocao'
import type { ModalAtivo } from './types'

type ModaisMesProps = {
  modalAtivo: ModalAtivo
  mesId: string
  onFechar: () => void
}

export function ModaisMes({ modalAtivo, mesId, onFechar }: ModaisMesProps) {
  return (
    <>
      <Modal
        aberto={modalAtivo?.tipo === 'criar'}
        titulo={() => modalAtivo?.tipo === 'criar' && modalAtivo?.tipoLancamento === 'DESPESA' ? 'Nova despesa' : 'Nova receita'}
        onFechar={onFechar}
      >
        {modalAtivo?.tipo === 'criar' && (
          <FormularioLancamento
            mesId={mesId}
            tipo={modalAtivo.tipoLancamento}
            onSucesso={onFechar}
            onCancelar={onFechar}
          />
        )}
      </Modal>

      <Modal
        aberto={modalAtivo?.tipo === 'editar'}
        titulo="Editar lançamento"
        onFechar={onFechar}
      >
        {modalAtivo?.tipo === 'editar' && (
          <FormularioLancamento
            mesId={mesId}
            tipo={modalAtivo.lancamento.tipo}
            lancamento={modalAtivo.lancamento}
            onSucesso={onFechar}
            onCancelar={onFechar}
          />
        )}
      </Modal>

      <Modal
        aberto={modalAtivo?.tipo === 'remover'}
        titulo="Remover lançamento"
        onFechar={onFechar}
      >
        {modalAtivo?.tipo === 'remover' && (
          <ConfirmacaoRemocao
            mesId={mesId}
            lancamento={modalAtivo.lancamento}
            onSucesso={onFechar}
            onCancelar={onFechar}
          />
        )}
      </Modal>
    </>
  )
}
