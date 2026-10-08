package br.ifsp.demo.reserva.exception;

import br.ifsp.demo.reserva.domain.Participante;

public class ParticipanteNaoEncontradoException extends RuntimeException {
    public ParticipanteNaoEncontradoException(Participante participante) {
        super("Participante " + participante.getNome() + " não faz parte da reserva.");
    }
}
