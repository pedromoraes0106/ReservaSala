package br.ifsp.demo.reserva.controller;

import br.ifsp.demo.reserva.domain.PeriodoReserva;
import br.ifsp.demo.reserva.domain.Reserva;
import br.ifsp.demo.reserva.domain.Sala;
import br.ifsp.demo.reserva.domain.StatusReserva;
import br.ifsp.demo.reserva.service.ReservaService;
import br.ifsp.demo.sala.service.SalaService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestControllerStructuralTest {

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveCriarReservaRetornandoCreated() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        UUID salaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 6, 9, 0);
        Reserva reserva = criarReserva(salaId, "Pedro", inicio, StatusReserva.CONFIRMADA);
        when(service.criarReserva(eq(salaId), eq("Pedro"), any(PeriodoReserva.class))).thenReturn(reserva);

        ResponseEntity<Reserva> response = controller.criar(
                new CriarReservaRequest(salaId, "Pedro", inicio, inicio.plusHours(1)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(reserva);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveConsultarReservasComFiltros() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        UUID salaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 6, 9, 0);
        LocalDateTime fim = inicio.plusHours(1);
        List<Reserva> reservas = List.of(criarReserva(salaId, "Pedro", inicio, StatusReserva.CONFIRMADA));
        when(service.consultarReservas(salaId, inicio, fim, "Pedro")).thenReturn(reservas);

        ResponseEntity<List<Reserva>> response = controller.consultar(salaId, inicio, fim, "Pedro");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(reservas);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRetornarDisponibilidadeDaSala() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        UUID salaId = UUID.randomUUID();
        LocalDate dia = LocalDate.of(2026, 11, 6);
        LocalDateTime inicio = dia.atTime(9, 0);
        LocalDateTime fim = inicio.plusHours(1);
        when(service.verificarDisponibilidade(salaId, dia, inicio, fim)).thenReturn(true);

        ResponseEntity<Boolean> response = controller.verificarDisponibilidade(salaId, dia, inicio, fim);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveEditarReservaRetornandoNoContent() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 6, 10, 0);
        EditarReservaRequest request = new EditarReservaRequest();
        request.setSalaId(salaId);
        request.setSolicitante("Maria");
        request.setInicio(inicio);
        request.setFim(inicio.plusHours(1));

        ResponseEntity<Void> response = controller.editar(reservaId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).editarReserva(any(Reserva.class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveListarReservasPorSolicitante() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        List<Reserva> reservas = List.of(criarReserva(
                UUID.randomUUID(), "Pedro", LocalDateTime.of(2026, 11, 6, 9, 0), StatusReserva.CONFIRMADA));
        when(service.listarPorSolicitante("Pedro")).thenReturn(reservas);

        ResponseEntity<List<Reserva>> response = controller.listarPorSolicitante("Pedro");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(reservas);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveAdicionarParticipanteNaReserva() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        UUID reservaId = UUID.randomUUID();
        Reserva reserva = criarReserva(UUID.randomUUID(), "Pedro", LocalDateTime.now(), StatusReserva.CONFIRMADA);
        AdicionarParticipanteRequest request = new AdicionarParticipanteRequest();
        request.setNome("Maria");
        when(service.adicionarParticipante(reservaId, "Maria")).thenReturn(reserva);

        ResponseEntity<Reserva> response = controller.adicionarParticipante(reservaId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(reserva);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRemoverParticipanteRetornandoNoContent() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        UUID reservaId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.removerParticipante(reservaId, "Maria");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).excluirParticipante(reservaId, "Maria");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveCancelarReserva() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        UUID reservaId = UUID.randomUUID();
        Reserva reserva = criarReserva(UUID.randomUUID(), "Pedro", LocalDateTime.now(), StatusReserva.CANCELADA);
        when(service.cancelarReserva(reservaId)).thenReturn(reserva);

        ResponseEntity<Reserva> response = controller.cancelar(reservaId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(reserva);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveConfirmarCheckIn() {
        ReservaService service = mock(ReservaService.class);
        ReservaController controller = new ReservaController(service);
        UUID reservaId = UUID.randomUUID();
        Reserva reserva = criarReserva(UUID.randomUUID(), "Pedro", LocalDateTime.now(), StatusReserva.EM_USO);
        when(service.confirmarCheckIn(eq(reservaId), any(LocalDateTime.class))).thenReturn(reserva);

        ResponseEntity<Reserva> response = controller.confirmarCheckIn(reservaId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(reserva);
        verify(service).confirmarCheckIn(eq(reservaId), any(LocalDateTime.class));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveCadastrarSalaRetornandoCreated() {
        SalaService service = mock(SalaService.class);
        SalaController controller = new SalaController(service);
        Sala sala = new Sala(UUID.randomUUID(), "Sala 01", 10);
        CadastrarSalaRequest request = new CadastrarSalaRequest();
        request.setNome("Sala 01");
        request.setCapacidade(10);
        when(service.cadastrarSala("Sala 01", 10)).thenReturn(sala);

        ResponseEntity<Sala> response = controller.cadastrar(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(sala);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveEditarSala() {
        SalaService service = mock(SalaService.class);
        SalaController controller = new SalaController(service);
        UUID salaId = UUID.randomUUID();
        Sala sala = new Sala(salaId, "Sala atualizada", 12);
        EditarSalaRequest request = new EditarSalaRequest();
        request.setNome("Sala atualizada");
        request.setCapacidade(12);
        when(service.editarSala(salaId, "Sala atualizada", 12)).thenReturn(sala);

        ResponseEntity<Sala> response = controller.editar(salaId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(sala);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveRemoverSalaRetornandoNoContent() {
        SalaService service = mock(SalaService.class);
        SalaController controller = new SalaController(service);
        UUID salaId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.remover(salaId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).removerSala(salaId);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void devePermitirLerECriarTodosOsDtosDeRequest() {
        UUID salaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 6, 9, 0);

        CriarReservaRequest criarReservaSemArgumentos = new CriarReservaRequest();
        criarReservaSemArgumentos.setSalaId(salaId);
        criarReservaSemArgumentos.setSolicitante("Pedro");
        criarReservaSemArgumentos.setInicio(inicio);
        criarReservaSemArgumentos.setFim(inicio.plusHours(1));
        assertThat(criarReservaSemArgumentos.getSalaId()).isEqualTo(salaId);
        assertThat(criarReservaSemArgumentos.getSolicitante()).isEqualTo("Pedro");
        assertThat(criarReservaSemArgumentos.getInicio()).isEqualTo(inicio);
        assertThat(criarReservaSemArgumentos.getFim()).isEqualTo(inicio.plusHours(1));

        CriarReservaRequest criarReserva = new CriarReservaRequest(salaId, "Pedro", inicio, inicio.plusHours(1));
        assertThat(criarReserva.getSalaId()).isEqualTo(salaId);
        assertThat(criarReserva.getSolicitante()).isEqualTo("Pedro");
        assertThat(criarReserva.getInicio()).isEqualTo(inicio);
        assertThat(criarReserva.getFim()).isEqualTo(inicio.plusHours(1));

        EditarReservaRequest editarReserva = new EditarReservaRequest();
        editarReserva.setSalaId(salaId);
        editarReserva.setSolicitante("Maria");
        editarReserva.setInicio(inicio);
        editarReserva.setFim(inicio.plusHours(2));
        assertThat(editarReserva.getSalaId()).isEqualTo(salaId);
        assertThat(editarReserva.getSolicitante()).isEqualTo("Maria");
        assertThat(editarReserva.getInicio()).isEqualTo(inicio);
        assertThat(editarReserva.getFim()).isEqualTo(inicio.plusHours(2));

        AdicionarParticipanteRequest adicionarParticipante = new AdicionarParticipanteRequest();
        adicionarParticipante.setNome("Joao");
        assertThat(adicionarParticipante.getNome()).isEqualTo("Joao");

        CadastrarSalaRequest cadastrarSala = new CadastrarSalaRequest();
        cadastrarSala.setNome("Sala nova");
        cadastrarSala.setCapacidade(8);
        assertThat(cadastrarSala.getNome()).isEqualTo("Sala nova");
        assertThat(cadastrarSala.getCapacidade()).isEqualTo(8);

        EditarSalaRequest editarSala = new EditarSalaRequest();
        editarSala.setNome("Sala editada");
        editarSala.setCapacidade(16);
        assertThat(editarSala.getNome()).isEqualTo("Sala editada");
        assertThat(editarSala.getCapacidade()).isEqualTo(16);
    }

    private Reserva criarReserva(UUID salaId, String solicitante, LocalDateTime inicio, StatusReserva status) {
        return new Reserva(
                UUID.randomUUID(),
                salaId,
                solicitante,
                new PeriodoReserva(inicio, inicio.plusHours(1)),
                status
        );
    }
}