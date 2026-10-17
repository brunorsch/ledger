package br.dev.brunorsch.ledger.server.exception

/** Lançamento inexistente ou pertencente a outro usuário. */
class LancamentoNaoEncontradoException : RuntimeException("Lançamento não encontrado para o usuário atual")
