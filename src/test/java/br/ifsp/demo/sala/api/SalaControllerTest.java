package br.ifsp.demo.sala.api;

import br.ifsp.demo.support.BaseApiIntegrationTest;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("ApiTest")
@Tag("IntegrationTest")
class SalaControllerTest extends BaseApiIntegrationTest {

    @Test
    @DisplayName("Deve cadastrar sala válida e retornar 201 com os dados no payload")
    void deveCadastrarSalaValidaERetornar201() {
        var token = registrarEAutenticar();
        var nome = "Sala " + faker.number().digits(5);

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of("nome", nome, "capacidade", 10))
                .when()
                .post("/api/salas")
                .then()
                .log().ifValidationFails(LogDetail.BODY)
                .statusCode(201)
                .body("id", notNullValue())
                .body("nome", equalTo(nome))
                .body("capacidade", equalTo(10));
    }

    @Test
    @DisplayName("Deve retornar 400 ao cadastrar sala com capacidade zero")
    void deveRetornar400ComCapacidadeZero() {
        var token = registrarEAutenticar();

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of("nome", "Sala " + faker.number().digits(5), "capacidade", 0))
                .when()
                .post("/api/salas")
                .then()
                .log().ifValidationFails(LogDetail.BODY)
                .statusCode(400);
    }

    @Test
    @DisplayName("Deve retornar 401 ao cadastrar sala sem token")
    void deveRetornar401SemToken() {
        given()
                .contentType("application/json")
                .body(Map.of("nome", "Sala " + faker.number().digits(5), "capacidade", 10))
                .when()
                .post("/api/salas")
                .then()
                .statusCode(401);
    }


}