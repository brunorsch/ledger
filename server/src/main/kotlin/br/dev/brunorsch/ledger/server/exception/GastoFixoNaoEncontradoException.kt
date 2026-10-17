package br.dev.brunorsch.ledger.server.exception

/** Gasto fixo inexistente ou pertencente a outro usuário. */
class GastoFixoNaoEncontradoException : RuntimeException("Gasto fixo não encontrado para o usuário atual")
