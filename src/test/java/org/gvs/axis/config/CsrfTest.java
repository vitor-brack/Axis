package org.gvs.axis.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gvs.axis.controller.HomeController;
import org.gvs.axis.controller.ReservaController;
import org.gvs.axis.dto.request.ReservaRequest;
import org.gvs.axis.dto.response.ReservaResponse;
import org.gvs.axis.model.Usuario;
import org.gvs.axis.security.CustomUserDetails;
import org.gvs.axis.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest({HomeController.class, ReservaController.class})
@Import(SecurityConfig.class)
class CsrfTest {

    private static final String CORPO = """
            {"ambienteId": 1, "horaInicio": "2030-01-15T10:00:00", "horaFim": "2030-01-15T11:00:00"}
            """;

    // Os scripts das páginas leem o token e o nome do header destas metatags do layout
    private static final Pattern META_TOKEN = Pattern.compile("<meta name=\"_csrf\" content=\"([^\"]+)\">");
    private static final Pattern META_HEADER = Pattern.compile("<meta name=\"_csrf_header\" content=\"([^\"]+)\">");

    private final CustomUserDetails ana = new CustomUserDetails(
            Usuario.builder().nome("Ana Souza").email("ana@exemplo.com").senha("hash").build(), List.of());

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservaService reservaService;

    @MockBean
    private UserDetailsService userDetailsService;

    // Bug: o CSRF estava desligado, e outro site podia criar reservas em nome de quem estivesse logado
    @Test
    void reservaSemTokenCsrfERecusada() throws Exception {
        mockMvc.perform(post("/reserva/criar")
                .with(user(ana))
                .contentType(MediaType.APPLICATION_JSON)
                .content(CORPO))
                .andExpect(status().isForbidden());

        verify(reservaService, never()).criarReserva(any(), any());
    }

    @Test
    void tokenDoCabecalhoDaPaginaAutorizaAReserva() throws Exception {
        when(reservaService.criarReserva(any(ReservaRequest.class), eq("ana@exemplo.com")))
                .thenReturn(new ReservaResponse());

        MvcResult pagina = mockMvc.perform(get("/").with(user(ana)))
                .andExpect(status().isOk())
                .andReturn();
        String html = pagina.getResponse().getContentAsString();
        String header = extrair(META_HEADER, html);
        String token = extrair(META_TOKEN, html);

        mockMvc.perform(post("/reserva/criar")
                .session((MockHttpSession) pagina.getRequest().getSession(false))
                .with(user(ana))
                .header(header, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CORPO))
                .andExpect(status().isOk());

        verify(reservaService).criarReserva(any(ReservaRequest.class), eq("ana@exemplo.com"));
    }

    private String extrair(Pattern meta, String html) {
        Matcher encontrado = meta.matcher(html);
        assertTrue(encontrado.find(), "metatag não encontrada: " + meta.pattern());
        return encontrado.group(1);
    }
}
