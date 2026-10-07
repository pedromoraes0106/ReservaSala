package br.ifsp.demo.reserva.domain;

import br.ifsp.demo.reserva.exception.PeriodoInvalidoException;

import java.time.LocalDateTime;
import java.util.Objects;

public class PeriodoReserva {
    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public PeriodoReserva(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new PeriodoInvalidoException("período inválido: a data final deve ser posterior à data inicial.");
        }
        this.inicio = inicio;
        this.fim = fim;
    }



    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeriodoReserva that)) return false;
        return Objects.equals(inicio, that.inicio) && Objects.equals(fim, that.fim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inicio, fim);
    }
}
