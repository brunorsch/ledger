package br.dev.brunorsch.ledger.server.service

/** Usuário autenticado pelo header do perfil local (antes do OAuth). */
data class UsuarioAutenticadoLocal(val usuarioId: Long)
