package org.gvs.axis.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.gvs.axis.dto.request.ReservaRequest;
import org.gvs.axis.dto.response.ReservaResponse;
import org.gvs.axis.service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author vitor
 */
@RestController
@RequestMapping("/reserva")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping("/criar")
    public ResponseEntity<ReservaResponse> criarReserva(
            @Valid @RequestBody ReservaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReservaResponse reserva = reservaService.criarReserva(request, userDetails.getUsername());
        return ResponseEntity.ok(reserva);
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listarReservasUsuario(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ReservaResponse> reservas = reservaService.buscarReservasDoUsuario(userDetails.getUsername());
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarReserva(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReservaResponse reserva = reservaService.buscarReserva(id, userDetails.getUsername());
        return ResponseEntity.ok(reserva);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelarReserva(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReservaResponse reserva = reservaService.cancelarReserva(id, userDetails.getUsername());
        return ResponseEntity.ok(reserva);
    }

    @GetMapping("/verificar-disponibilidade")
    public ResponseEntity<Boolean> verificarDisponibilidade(
            @RequestParam Long ambienteId,
            @RequestParam String data,
            @RequestParam String horaInicio,
            @RequestParam String horaFim) {

        boolean disponivel = reservaService.verificarDisponibilidade(
                ambienteId,
                LocalDateTime.parse(data),
                LocalTime.parse(horaInicio),
                LocalTime.parse(horaFim)
        );

        return ResponseEntity.ok(disponivel);
    }
}
