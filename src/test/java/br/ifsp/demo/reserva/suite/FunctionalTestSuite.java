package br.ifsp.demo.reserva.suite;

import br.ifsp.demo.reserva.service.ReservaServiceFunctionalTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses(ReservaServiceFunctionalTest.class)
public class FunctionalTestSuite {
}
