package br.ifsp.demo.reserva.controller;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
