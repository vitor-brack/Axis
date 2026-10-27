package org.gvs.axis.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.gvs.axis.enums.StatusReserva;
import org.gvs.axis.model.Reserva;
import org.gvs.axis.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author vitor
 */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioAndStatusInOrderByDataCriacaoDesc(Usuario usuario, List<StatusReserva> status);

    List<Reserva> findByUsuarioEmailOrderByDataCriacaoDescHoraInicioDesc(String email);

    List<Reserva> findByAmbienteIdAndDataCriacao(Long ambiente, LocalDateTime dataCriacao);

    @Query("SELECT r FROM Reserva r WHERE r.id = :id AND r.usuario.email = :email")
    Optional<Reserva> findByIdAndUsuarioEmail(Long id, String email);

    @Query("SELECT CASE WHEN COUNT(r) = 0 THEN true ELSE false END FROM Reserva r "
            + "WHERE r.ambiente.id = :ambienteId AND r.status != 'CANCELADA' AND "
            + "((r.horaInicio <= :fim AND r.horaFim >= :inicio))")
    boolean isAmbienteDisponivel(
            @Param("ambienteId") Long ambienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("SELECT new list(r.horaInicio, r.horaFim) FROM Reserva r "
            + "WHERE r.ambiente.id = :ambienteId AND r.status != 'CANCELADA' AND "
            + "r.horaInicio >= :dataInicio AND r.horaInicio < :dataFim "
            + "ORDER BY r.horaInicio")
    List<LocalDateTime[]> findHorariosOcupados(
            @Param("ambienteId") Long ambienteId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT r FROM Reserva r "
            + "WHERE r.ambiente.id = :ambienteId "
            + "AND r.dataCriacao = :data "
            + "AND r.status != 'CANCELADA' "
            + "AND ((r.horaInicio <= :horaFim AND r.horaFim >= :horaInicio) "
            + "OR (r.horaInicio >= :horaInicio AND r.horaInicio < :horaFim)) ")
    List<Reserva> findConflitantes(
            @Param("ambienteId") Long ambienteId,
            @Param("data") LocalDateTime data,
            @Param("horaInicio") LocalDateTime horaInicio,
            @Param("horaFim") LocalDateTime horaFim
    );

    @Query("UPDATE Reserva r "
            + "SET r.status = 'CONCLUIDA' "
            + "WHERE r.status = 'CONFIRMADA' "
            + "AND r.horaInicio < :dataAtual")
    @Modifying
    @Transactional
    int atualizarStatusReservasExpiradas(
            @Param("dataAtual") LocalDateTime dataAtual
    );

    @Query("SELECT r FROM Reserva r "
            + "WHERE r.ambiente.id = :ambienteId "
            + "AND CAST(r.horaInicio AS date) = CAST(:horaInicio AS date) "
            + "AND (r.status = 'CONFIRMADA' OR r.status = 'CONCLUIDA') "
            + "AND r.id != :excluirReservaId "
            + "AND ("
            + "    (r.horaInicio < :horaFim AND r.horaFim > :horaInicio) OR "
            + "    (r.horaInicio = :horaInicio) OR "
            + "    (r.horaFim = :horaFim)"
            + ")")
    List<Reserva> findReservasSobrepostas(
            @Param("ambienteId") Long ambienteId,
            @Param("horaInicio") LocalDateTime horaInicio,
            @Param("horaFim") LocalDateTime horaFim,
            @Param("excluirReservaId") Long excluirReservaId
    );
}
