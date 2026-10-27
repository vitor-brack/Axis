package org.gvs.axis.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.gvs.axis.dto.request.ReservaRequest;
import org.gvs.axis.dto.response.ReservaResponse;
import org.gvs.axis.enums.StatusReserva;
import org.gvs.axis.model.Ambiente;
import org.gvs.axis.model.Reserva;
import org.gvs.axis.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
@Import(ReservaService.class)
class ReservaServiceTest {

    private static final LocalDate DIA = LocalDate.now().plusDays(7);

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private TestEntityManager em;

    private Usuario ana;
    private Usuario bruno;
    private Ambiente sala;

    @BeforeEach
    void criarDados() {
        ana = em.persist(Usuario.builder().nome("Ana Souza").email("ana@exemplo.com").senha("hash").build());
        bruno = em.persist(Usuario.builder().nome("Bruno Lima").email("bruno@exemplo.com").senha("hash").build());
        sala = em.persist(Ambiente.builder().nome("Sala 1").capacidade(4).valorHora(new BigDecimal("50.00")).build());
    }

    // Bug: criarReserva salvava sem procurar conflito, e duas pessoas reservavam a mesma sala no mesmo horário
    @Test
    void criarReservaRecusaHorarioSobreposto() {
        reservaExistente(bruno, LocalTime.of(10, 0), LocalTime.of(11, 0), StatusReserva.CONFIRMADA);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> reservaService.criarReserva(pedido(LocalTime.of(10, 30), LocalTime.of(11, 30)), "ana@exemplo.com"));

        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        assertEquals(1L, contarReservas());
    }

    @Test
    void criarReservaAceitaHorarioLivre() {
        reservaExistente(bruno, LocalTime.of(10, 0), LocalTime.of(11, 0), StatusReserva.CONFIRMADA);

        ReservaResponse criada = reservaService.criarReserva(pedido(LocalTime.of(13, 0), LocalTime.of(14, 0)), "ana@exemplo.com");

        assertEquals(StatusReserva.CONFIRMADA, criada.getStatus());
        assertEquals(2L, contarReservas());
    }

    @Test
    void reservaCanceladaNaoBloqueiaOHorario() {
        reservaExistente(bruno, LocalTime.of(10, 0), LocalTime.of(11, 0), StatusReserva.CANCELADA);

        ReservaResponse criada = reservaService.criarReserva(pedido(LocalTime.of(10, 0), LocalTime.of(11, 0)), "ana@exemplo.com");

        assertEquals(StatusReserva.CONFIRMADA, criada.getStatus());
    }

    // criarReserva grava o fim com 30 minutos a mais; as reservas de teste seguem a mesma regra
    private Reserva reservaExistente(Usuario usuario, LocalTime inicio, LocalTime fim, StatusReserva status) {
        return em.persist(Reserva.builder()
                .usuario(usuario)
                .ambiente(sala)
                .horaInicio(DIA.atTime(inicio))
                .horaFim(DIA.atTime(fim).plusMinutes(30))
                .valorTotal(new BigDecimal("50.00"))
                .status(status)
                .build());
    }

    private ReservaRequest pedido(LocalTime inicio, LocalTime fim) {
        ReservaRequest pedido = new ReservaRequest();
        pedido.setAmbienteId(sala.getId());
        pedido.setHoraInicio(DIA.atTime(inicio));
        pedido.setHoraFim(DIA.atTime(fim));
        return pedido;
    }

    private long contarReservas() {
        return em.getEntityManager()
                .createQuery("SELECT COUNT(r) FROM Reserva r", Long.class)
                .getSingleResult();
    }
}
