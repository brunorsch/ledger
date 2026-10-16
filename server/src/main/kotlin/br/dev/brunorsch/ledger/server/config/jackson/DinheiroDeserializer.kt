package br.dev.brunorsch.ledger.server.config.jackson

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import org.springframework.boot.jackson.JacksonComponent
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

/**
 * Desserializa [Dinheiro] a partir de string no JSON (ex.: `"1193.08"`),
 * aplicando a normalização e as validações do tipo.
 */
@JacksonComponent
class DinheiroDeserializer : ValueDeserializer<Dinheiro>() {

	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Dinheiro =
		Dinheiro(p.getString())
}
