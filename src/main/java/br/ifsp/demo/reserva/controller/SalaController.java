package br.ifsp.demo.reserva.controller;

import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.service.SalaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    public ResponseEntity<Sala> cadastrar(@RequestBody CadastrarSalaRequest request) {
        Sala sala = salaService.cadastrarSala(request.getNome(), request.getCapacidade());
        return ResponseEntity.status(HttpStatus.CREATED).body(sala);
    }

    @PutMapping("/{salaId}")
    public ResponseEntity<Sala> editar(
            @PathVariable UUID salaId,
            @RequestBody EditarSalaRequest request) {
        return ResponseEntity.ok(salaService.editarSala(salaId, request.getNome(), request.getCapacidade()));
    }

    @DeleteMapping("/{salaId}")
    public ResponseEntity<Void> remover(@PathVariable UUID salaId) {
        salaService.removerSala(salaId);
        return ResponseEntity.noContent().build();
    }
}