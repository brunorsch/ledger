package br.dev.brunorsch.ledger.server.exception

class MesJaExisteException(mesReferencia: String) :
	RuntimeException("mês já existe para a referência: $mesReferencia")
