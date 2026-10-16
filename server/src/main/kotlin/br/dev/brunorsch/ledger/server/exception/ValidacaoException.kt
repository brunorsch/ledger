package br.dev.brunorsch.ledger.server.exception

/** Requisição inválida (400) com mensagem clara para quem consome a API. */
class ValidacaoException(message: String) : RuntimeException(message)
