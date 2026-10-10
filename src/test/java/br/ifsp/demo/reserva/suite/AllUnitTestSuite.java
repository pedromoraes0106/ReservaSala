package br.ifsp.demo.reserva.suite;

import br.ifsp.demo.exception.ApiExceptionHandlerTddTest;
import br.ifsp.demo.sala.domain.SalaTest;
import br.ifsp.demo.service.ReservaServiceTest;
import br.ifsp.demo.service.SalaServiceTest;
import br.ifsp.demo.reserva.service.ReservaServiceFunctionalTest;
import br.ifsp.demo.reserva.service.ReservaServiceTddTest;
import br.ifsp.demo.reserva.service.SalaServiceTddTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
	ReservaServiceTddTest.class,
	SalaServiceTddTest.class,
	ApiExceptionHandlerTddTest.class,
	ReservaServiceFunctionalTest.class,
	ReservaServiceTest.class,
	SalaServiceTest.class,
	SalaTest.class
})
public class AllUnitTestSuite {
}
