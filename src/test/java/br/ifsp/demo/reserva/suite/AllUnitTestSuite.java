package br.ifsp.demo.reserva.suite;

import br.ifsp.demo.exception.ApiExceptionHandlerTddTest;
import br.ifsp.demo.reserva.service.ReservaServiceFunctionalTest;
import br.ifsp.demo.reserva.service.ReservaServiceTddTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ReservaServiceTddTest.class, ReservaServiceFunctionalTest.class, ApiExceptionHandlerTddTest.class})
public class AllUnitTestSuite {
}
