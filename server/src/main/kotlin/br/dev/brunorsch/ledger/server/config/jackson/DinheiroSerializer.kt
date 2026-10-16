package br.dev.brunorsch.ledger.server.config.jackson

import br.dev.brunorsch.ledger.server.domain.Dinheiro
import org.springframework.boot.jackson.JacksonComponent
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

/**
 * Serializa [Dinheiro] como string no JSON (ex.: `"1193.08"`), para o
 * JavaScript não converter o valor para `double`.
 */
@JacksonComponent
class DinheiroSerializer : ValueSerializer<Dinheiro>() {

	override fun serialize(value: Dinheiro, gen: JsonGenerator, ctxt: SerializationContext) {
		gen.writeString(value.valor.toPlainString())
	}
}
