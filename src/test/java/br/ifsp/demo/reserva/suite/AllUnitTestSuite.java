package br.ifsp.demo.reserva.suite;

import br.ifsp.demo.exception.ApiExceptionHandlerTddTest;
import br.ifsp.demo.exception.ExceptionStructuralTest;
import br.ifsp.demo.reserva.domain.SalaTest;
import br.ifsp.demo.reserva.service.*;
import br.ifsp.demo.sala.service.SalaServiceStructuralTest;
import br.ifsp.demo.reserva.controller.RestControllerStructuralTest;
import br.ifsp.demo.controller.TransactionControllerStructuralTest;
import br.ifsp.demo.reserva.repository.RepositoryMutationTest;
import br.ifsp.demo.reserva.repository.RepositoryStructuralTest;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        ReservaServiceTddTest.class,
        SalaServiceTddTest.class,
        ApiExceptionHandlerTddTest.class,
        ReservaServiceFunctionalTest.class,
        ReservaDomainStructuralTest.class,
        ReservaServiceStructuralTest.class,
        SalaServiceStructuralTest.class,
        RestControllerStructuralTest.class,
        TransactionControllerStructuralTest.class,
        RepositoryStructuralTest.class,
        RepositoryMutationTest.class,
        ExceptionStructuralTest.class,
        SalaTest.class
})
public class AllUnitTestSuite {
}