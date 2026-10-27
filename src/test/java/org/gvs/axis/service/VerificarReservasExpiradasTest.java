package org.gvs.axis.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.gvs.axis.enums.StatusReserva;
import org.gvs.axis.model.Ambiente;
import org.gvs.axis.model.Reserva;
import org.gvs.axis.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

// Sem a transação do teste, para o serviço abrir e fechar as próprias transações como em produção
@DataJpaTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
@Import(ReservaService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ExtendWith(OutputCaptureExtension.class)
class VerificarReservasExpiradasTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PlatformTransactionManager transacoes;

    // Sem a transação do teste, nada volta sozinho; os outros testes esperam o banco vazio
    @AfterEach
    void limparBanco() {
        jdbc.update("DELETE FROM reservas");
        jdbc.update("DELETE FROM ambientes");
        jdbc.update("DELETE FROM usuarios");
    }

    @Test
    void reservaConfirmadaQueJaComecouViraConcluida() {
        Long id = new TransactionTemplate(transacoes).execute(status -> {
            Usuario ana = em.persist(Usuario.builder().nome("Ana Souza").email("ana@exemplo.com").senha("hash").build());
            Ambiente sala = em.persist(Ambiente.builder().nome("Sala 1").capacidade(4).valorHora(new BigDecimal("50.00")).build());
            return em.persist(Reserva.builder()
                    .usuario(ana)
                    .ambiente(sala)
                    .horaInicio(LocalDateTime.now().minusHours(2))
                    .horaFim(LocalDateTime.now().minusMinutes(30))
                    .valorTotal(new BigDecimal("50.00"))
                    .status(StatusReserva.CONFIRMADA)
                    .build()).getId();
        });

        reservaService.verificarReservasExpiradas();

        assertEquals("CONCLUIDA", jdbc.queryForObject("SELECT status FROM reservas WHERE id = ?", String.class, id));
    }

    // Bug: o agendamento lançava ResponseStatusException, que nenhuma requisição HTTP recebe, e descartava a causa
    @Test
    void falhaNoBancoViraLogComACausa(CapturedOutput saida) {
        // Simula o banco recusando o UPDATE: a tabela sai do lugar durante a execução
        jdbc.execute("ALTER TABLE reservas RENAME TO reservas_fora_do_ar");
        try {
            assertDoesNotThrow(() -> reservaService.verificarReservasExpiradas());
        } finally {
            jdbc.execute("ALTER TABLE reservas_fora_do_ar RENAME TO reservas");
        }

        assertTrue(saida.getAll().contains("Erro ao verificar reservas expiradas"));
        assertTrue(saida.getAll().contains("Table \"RESERVAS\" not found"));
    }
}
