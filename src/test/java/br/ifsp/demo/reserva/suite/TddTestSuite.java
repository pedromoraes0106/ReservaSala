package br.ifsp.demo.reserva.suite;

import br.ifsp.demo.reserva.service.ReservaServiceTddTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses(ReservaServiceTddTest.class)
public class TddTestSuite {
}
