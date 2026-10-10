package br.ifsp.demo.reserva.controller;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<Reserva> criar(@RequestBody CriarReservaRequest request) {
        PeriodoReserva periodo = new PeriodoReserva(request.getInicio(), request.getFim());
        Reserva reserva = reservaService.criarReserva(request.getSalaId(), request.getSolicitante(), periodo);
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    @GetMapping
    public ResponseEntity<List<Reserva>> consultar(
            @RequestParam(required = false) UUID salaId,
            @RequestParam(required = false) LocalDateTime inicio,
            @RequestParam(required = false) LocalDateTime fim,
            @RequestParam(required = false) String solicitante) {
        return ResponseEntity.ok(reservaService.consultarReservas(salaId, inicio, fim, solicitante));
    }

    @GetMapping("/solicitante/{solicitante}")
    public ResponseEntity<List<Reserva>> listarPorSolicitante(@PathVariable String solicitante) {
        return ResponseEntity.ok(reservaService.listarPorSolicitante(solicitante));
    }

    @PostMapping("/{reservaId}/participantes")
    public ResponseEntity<Reserva> adicionarParticipante(
            @PathVariable UUID reservaId,
            @RequestBody AdicionarParticipanteRequest request) {
        Reserva reserva = reservaService.adicionarParticipante(reservaId, request.getNome());
        return ResponseEntity.ok(reserva);
    }

    @PostMapping("/{reservaId}/cancelamento")
    public ResponseEntity<Reserva> cancelar(@PathVariable UUID reservaId) {
        return ResponseEntity.ok(reservaService.cancelarReserva(reservaId));
    }

    @PostMapping("/{reservaId}/check-in")
    public ResponseEntity<Reserva> confirmarCheckIn(@PathVariable UUID reservaId) {
        return ResponseEntity.ok(reservaService.confirmarCheckIn(reservaId, LocalDateTime.now()));
    }
}
