package org.gvs.axis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.gvs.axis.config.SecurityConfig;
import org.gvs.axis.dto.request.ReservaRequest;
import org.gvs.axis.dto.response.ReservaResponse;
import org.gvs.axis.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservaController.class)
@Import(SecurityConfig.class)
class ReservaControllerTest {

    private static final String CORPO = """
            {"ambienteId": 1, "horaInicio": "2030-01-15T10:00:00", "horaFim": "2030-01-15T11:00:00"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservaService reservaService;

    @MockBean
    private UserDetailsService userDetailsService;

    // Bug: /reserva/criar estava no permitAll, e o anônimo chegava ao controller com userDetails nulo (NullPointerException)
    @Test
    void usuarioAnonimoNaoCriaReserva() throws Exception {
        mockMvc.perform(post("/reserva/criar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CORPO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/usuario/login"));

        verify(reservaService, never()).criarReserva(any(), any());
    }

    @Test
    void usuarioLogadoCriaReservaNoProprioEmail() throws Exception {
        when(reservaService.criarReserva(any(ReservaRequest.class), eq("ana@exemplo.com")))
                .thenReturn(new ReservaResponse());

        mockMvc.perform(post("/reserva/criar")
                .with(user("ana@exemplo.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CORPO))
                .andExpect(status().isOk());

        verify(reservaService).criarReserva(any(ReservaRequest.class), eq("ana@exemplo.com"));
    }
}
