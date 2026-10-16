package br.dev.brunorsch.ledger.server.exception

/** Mês inexistente ou pertencente a outro usuário. */
class MesNaoEncontradoException : RuntimeException("Mês não encontrado para o usuário atual")
