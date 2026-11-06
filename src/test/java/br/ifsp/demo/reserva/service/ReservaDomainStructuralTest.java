package br.ifsp.demo.reserva.service;

import br.ifsp.demo.reserva.domain.*;
import br.ifsp.demo.reserva.exception.PeriodoInvalidoException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReservaDomainStructuralTest {
    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarIllegalArgumentExceptionSeIdDaSalaForNulo(){
        assertThatThrownBy(()->new Sala(null,"teste",10)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("identificador da sala é obrigatório.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarIllegalArgumentExceptionSeNomeDaSalaForNulo(){
        assertThatThrownBy(()->new Sala(UUID.randomUUID(),null,10)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nome da sala é obrigatório.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarIllegalArgumentExceptionSeNomeDaSalaForBranco(){
        assertThatThrownBy(()->new Sala(UUID.randomUUID(),"",10)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nome da sala é obrigatório.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarIllegalArgumentExceptionSeCapacidadeDaForInvalida(){
        assertThatThrownBy(()->new Sala(UUID.randomUUID(),"teste",-1)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("capacidade da sala deve ser maior que zero.");
    }

    @Nested
    @Tag("UnitTest")
    @Tag("Structural")
    class Equals{

        @Nested
        class SalaEquals{
            @Test
            void deveSerIgualQuandoForAMesmaInstancia() {
                Sala sala = new Sala(UUID.randomUUID(), "lab1", 10);

                assertThat(sala.equals(sala)).isTrue();
            }

            @Test
            void naoDeveSerIgualQuandoForNulo() {
                Sala sala = new Sala(UUID.randomUUID(), "lab1", 10);

                assertThat(sala.equals(null)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoForDeOutroTipo() {
                Sala sala = new Sala(UUID.randomUUID(), "lab1", 10);

                assertThat(sala.equals("lab1")).isFalse();
            }

            @Test
            void deveSerIgualQuandoIdECapacidadeForemIguais() {
                UUID salaId = UUID.randomUUID();
                Sala sala1 = new Sala(salaId, "lab1", 10);
                Sala sala2 = new Sala(salaId, "lab1", 10);

                assertThat(sala1.equals(sala2)).isTrue();
            }

            @Test
            void naoDeveSerIgualQuandoCapacidadeForDiferente() {
                UUID salaId = UUID.randomUUID();
                Sala sala1 = new Sala(salaId, "lab1", 10);
                Sala sala2 = new Sala(salaId, "lab1", 20);

                assertThat(sala1.equals(sala2)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoIdForDiferente() {
                Sala sala1 = new Sala(UUID.randomUUID(), "lab1", 10);
                Sala sala2 = new Sala(UUID.randomUUID(), "lab1", 10);

                assertThat(sala1.equals(sala2)).isFalse();
            }
        }

        @Nested
        class PeriodoReservaEquals{
            private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 10, 8, 0);
            private static final LocalDateTime FIM = LocalDateTime.of(2026, 11, 10, 10, 0);

            @Test
            void deveSerIgualQuandoForAMesmaInstancia() {
                PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);

                assertThat(periodo.equals(periodo)).isTrue();
            }

            @Test
            void naoDeveSerIgualQuandoForNulo() {
                PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);

                assertThat(periodo.equals(null)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoForDeOutroTipo() {
                PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);

                assertThat(periodo.equals("periodo")).isFalse();
            }

            @Test
            void deveSerIgualQuandoInicioEFimForemIguais() {
                PeriodoReserva periodo1 = new PeriodoReserva(INICIO, FIM);
                PeriodoReserva periodo2 = new PeriodoReserva(INICIO, FIM);

                assertThat(periodo1.equals(periodo2)).isTrue();
            }

            @Test
            void naoDeveSerIgualQuandoInicioForDiferente() {
                PeriodoReserva periodo1 = new PeriodoReserva(INICIO, FIM);
                PeriodoReserva periodo2 = new PeriodoReserva(INICIO.minusHours(1), FIM);

                assertThat(periodo1.equals(periodo2)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoFimForDiferente() {
                PeriodoReserva periodo1 = new PeriodoReserva(INICIO, FIM);
                PeriodoReserva periodo2 = new PeriodoReserva(INICIO, FIM.plusHours(1));

                assertThat(periodo1.equals(periodo2)).isFalse();
            }

        }

        @Nested
        class ReservaEquals {
            private final UUID id = UUID.randomUUID();
            private final UUID salaId = UUID.randomUUID();
            private final PeriodoReserva periodo = new PeriodoReserva(
                    LocalDateTime.of(2026, 11, 10, 8, 0),
                    LocalDateTime.of(2026, 11, 10, 10, 0));
            private final List<Participante> participantes = List.of(new Participante("Bruno"));

            private Reserva reservaBase() {
                return new Reserva(id, salaId, "Ana", periodo, StatusReserva.CONFIRMADA, participantes);
            }

            @Test
            void deveSerIgualQuandoForAMesmaInstancia() {
                Reserva reserva = reservaBase();

                assertThat(reserva.equals(reserva)).isTrue();
            }

            @Test
            void naoDeveSerIgualQuandoForNulo() {
                assertThat(reservaBase().equals(null)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoForDeOutroTipo() {
                assertThat(reservaBase().equals("reserva")).isFalse();
            }

            @Test
            void deveSerIgualQuandoTodosOsCamposForemIguais() {
                assertThat(reservaBase().equals(reservaBase())).isTrue();
            }

            @Test
            void naoDeveSerIgualQuandoIdForDiferente() {
                Reserva outra = new Reserva(UUID.randomUUID(), salaId, "Ana", periodo, StatusReserva.CONFIRMADA, participantes);

                assertThat(reservaBase().equals(outra)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoSalaIdForDiferente() {
                Reserva outra = new Reserva(id, UUID.randomUUID(), "Ana", periodo, StatusReserva.CONFIRMADA, participantes);

                assertThat(reservaBase().equals(outra)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoSolicitanteForDiferente() {
                Reserva outra = new Reserva(id, salaId, "Pedro", periodo, StatusReserva.CONFIRMADA, participantes);

                assertThat(reservaBase().equals(outra)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoPeriodoForDiferente() {
                PeriodoReserva outroPeriodo = new PeriodoReserva(
                        LocalDateTime.of(2026, 11, 11, 8, 0),
                        LocalDateTime.of(2026, 11, 11, 10, 0));
                Reserva outra = new Reserva(id, salaId, "Ana", outroPeriodo, StatusReserva.CONFIRMADA, participantes);

                assertThat(reservaBase().equals(outra)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoStatusForDiferente() {
                Reserva outra = new Reserva(id, salaId, "Ana", periodo, StatusReserva.CANCELADA, participantes);

                assertThat(reservaBase().equals(outra)).isFalse();
            }

            @Test
            void naoDeveSerIgualQuandoParticipantesForemDiferentes() {
                Reserva outra = new Reserva(id, salaId, "Ana", periodo, StatusReserva.CONFIRMADA, List.of());

                assertThat(reservaBase().equals(outra)).isFalse();
            }
        }

    }

    @Nested
    @Tag("UnitTest")
    @Tag("Structural")
    class HashCode {

        @Nested
        class SalaHashCode{
            @Test
            void deveTerMesmoHashQuandoSalasForemIguais() {
                UUID uuid = UUID.randomUUID();
                Sala sala1 = new Sala(uuid, "lab1", 10);
                Sala sala2 = new Sala(uuid, "lab1", 10);

                assertThat(sala1.hashCode()).isEqualTo(sala2.hashCode());
            }

            @Test
            void deveTerHashDiferenteQuandoSalasForemDiferentes() {

                Sala sala1 = new Sala(UUID.randomUUID(), "lab1", 10);
                Sala sala2 = new Sala(UUID.randomUUID(), "lab2", 20);

                assertThat(sala1.hashCode()).isNotEqualTo(sala2.hashCode());
            }
        }

        @Nested
        class PeriodoReservaHashCode{
            private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 10, 8, 0);
            private static final LocalDateTime FIM = LocalDateTime.of(2026, 11, 10, 10, 0);

            @Test
            void deveTerMesmoHashQuandoPeriodosForemIguais() {
                PeriodoReserva periodo1 = new PeriodoReserva(INICIO, FIM);
                PeriodoReserva periodo2 = new PeriodoReserva(INICIO, FIM);

                assertThat(periodo1.hashCode()).isEqualTo(periodo2.hashCode());
            }

            @Test
            void deveTerHashDiferenteQuandoPeriodosForemDiferentes() {
                PeriodoReserva periodo1 = new PeriodoReserva(INICIO, FIM);
                PeriodoReserva periodo2 = new PeriodoReserva(INICIO.plusDays(1), FIM.plusDays(1));

                assertThat(periodo1.hashCode()).isNotEqualTo(periodo2.hashCode());
            }
        }
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarPeriodoInvalidoExceptionQuandoInicioForNulo() {
        assertThatThrownBy(() -> new PeriodoReserva(null, LocalDateTime.of(2026, 11, 10, 10, 0)))
                .isInstanceOf(PeriodoInvalidoException.class)
                .hasMessageContaining("período inválido");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarPeriodoInvalidoExceptionQuandoFimForNulo() {
        assertThatThrownBy(() -> new PeriodoReserva(LocalDateTime.of(2026, 11, 10, 10, 0),null))
                .isInstanceOf(PeriodoInvalidoException.class)
                .hasMessageContaining("período inválido");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveAlterarStatusDaReserva() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 20, 9, 0);
        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Pedro",
                new PeriodoReserva(inicio, inicio.plusHours(1)),
                StatusReserva.CONFIRMADA);

        reserva.setStatus(StatusReserva.CANCELADA);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADA);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    void deveLancarIllegalArgumentExceptionSeIdParticipanteForNuloAoAdicionar(){
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 20, 9, 0);
        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Pedro",
                new PeriodoReserva(inicio, inicio.plusHours(1)),
                StatusReserva.CONFIRMADA);

        assertThatThrownBy(()->reserva.adicionarParticipante(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("participante é obrigatório.");
    }




}
