package br.dev.brunorsch.ledger.server.exception

/** Usuário do header de autenticação não existe no MVP (antes do OAuth). */
class UsuarioNaoEncontradoException(usuarioId: Long) :
	RuntimeException("Usuário do header X-Usuario-Id não encontrado: $usuarioId")
