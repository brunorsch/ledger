export type MesResponse = {
  id: string
  mesReferencia: string
  parametrosCalculo: ParametrosCalculoResponse
  alternarInvestir: boolean
  alternarLazer: boolean
  deficit: string
  criadoEm: string
}

export type ParametrosCalculoResponse = {
  versaoEsquema: number
  percentuais: {
    essenciais: number
    lazer: number
  }
  tetos: {
    essenciais: string
    lazer: string
  }
}

export type MesSumarioResponse = {
  receitas: string
  despesas: string
  aPagar: string
  restante: string
  distribuicao: DistribuicaoSobrasResponse
}

export type DistribuicaoSobrasResponse = {
  essenciais: string
  lazer: string
  reservas: string
  deficit: string
}

export type LancamentoDespesaResponse = {
  id: string
  tipo: 'DESPESA'
  descricao: string
  valor: string
  pago: boolean
  pagoEm: string | null
  origemGastoFixo: boolean
}

export type LancamentoReceitaResponse = {
  id: string
  tipo: 'RECEITA'
  descricao: string
  valor: string
}

export type LancamentoResponse = LancamentoDespesaResponse | LancamentoReceitaResponse

export type CriarLancamentoCorpo = {
  tipo: 'DESPESA' | 'RECEITA'
  descricao: string
  valor: string
}

export type AlterarLancamentoCorpo = {
  descricao?: string
  valor?: string
  pago?: boolean
}

export type AlterarAlternanciasCorpo = {
  alternarInvestir?: boolean
  alternarLazer?: boolean
}

export type AlternanciasResponse = {
  alternarInvestir: boolean
  alternarLazer: boolean
}

export type CriarMesCorpo = {
  mesReferencia: string
  parametrosCalculo: {
    versaoEsquema: number
    percentuais: {
      essenciais: number
      lazer: number
    }
    tetos: {
      essenciais: string
      lazer: string
    }
  }
  alternarInvestir: boolean
  alternarLazer: boolean
}

export type GastoFixoResponse = {
  id: string
  descricao: string
  valorPadrao: string
  ativo: boolean
}

export type CriarGastoFixoCorpo = {
  descricao: string
  valorPadrao: string
}

export type AlterarGastoFixoCorpo = {
  descricao?: string
  valorPadrao?: string
  ativo?: boolean
}

export class ErroApi extends Error {
  readonly status: number
  readonly mensagem: string

  constructor(message: string, status: number) {
    super(message)
    this.mensagem = message
    this.status = status
    this.name = 'ErroApi'
  }
}
