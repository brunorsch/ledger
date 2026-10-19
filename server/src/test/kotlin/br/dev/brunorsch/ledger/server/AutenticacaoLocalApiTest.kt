package br.dev.brunorsch.ledger.server

import br.dev.brunorsch.ledger.server.service.UsuarioAutenticadoService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class AutenticacaoLocalApiTest : SuportePostgres() {

	@Autowired
	lateinit var mockMvc: MockMvc

	// No perfil local a API dispensa token e aceita o header X-Usuario-Id.
	@Test
	fun `perfil local aceita header x usuario id`() {
		mockMvc.get("/api/v1/gastos-fixos") {
			header(UsuarioAutenticadoService.HEADER_USUARIO_ID, "1")
		}
			.andExpect { status { isOk() } }
	}

	// Sem o header vale o usuário seed.
	@Test
	fun `perfil local sem header usa o usuario seed`() {
		mockMvc.get("/api/v1/gastos-fixos")
			.andExpect { status { isOk() } }
	}
}
