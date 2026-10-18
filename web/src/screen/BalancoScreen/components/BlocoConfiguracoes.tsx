import { useState } from 'react'
import { Box, Interruptor } from '@/components'
import { useAlterarAlternancias } from '@/api/queries'
import { ErroApi, type MesResponse } from '@/api/types'

type BlocoConfiguracoesProps = {
  mes: MesResponse
}

export function BlocoConfiguracoes({ mes }: BlocoConfiguracoesProps) {
  const alternar = useAlterarAlternancias(mes.id)
  const [erro, setErro] = useState<string | null>(null)

  const alternarInvestir = Boolean(mes.alternarInvestir)
  const alternarLazer = Boolean(mes.alternarLazer)

  async function aoAlternarInvestir(ligado: boolean) {
    setErro(null)

    try {
      await alternar.mutateAsync({ alternarInvestir: ligado })
    } catch (err) {
      setErro(mensagemDeErroAoAlternar(err))
    }
  }

  async function aoAlternarLazer(ligado: boolean) {
    setErro(null)

    try {
      await alternar.mutateAsync({ alternarLazer: ligado })
    } catch (err) {
      setErro(mensagemDeErroAoAlternar(err))
    }
  }

  return (
    <Box titulo="Configurações">
      <div className="space-y-4 px-5 py-4 md:px-6">
        <Interruptor
          rotulo="Investir"
          descricao="Reservar parte do restante para investimentos."
          checked={alternarInvestir}
          disabled={alternar.isPending}
          onChange={aoAlternarInvestir}
        />
        <Interruptor
          rotulo="Lazer"
          descricao="Separar parcela do restante para lazer."
          checked={alternarLazer}
          disabled={alternar.isPending}
          onChange={aoAlternarLazer}
        />
        {erro && (
          <p className="text-sm text-danger" role="alert">
            {erro}
          </p>
        )}
      </div>
    </Box>
  )
}

function mensagemDeErroAoAlternar(err: unknown): string {
  if (err instanceof ErroApi) return err.mensagem
  return 'Não foi possível salvar. Tente novamente.'
}
