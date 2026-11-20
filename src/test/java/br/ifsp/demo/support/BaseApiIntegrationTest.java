package br.ifsp.demo.support;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseApiIntegrationTest {

    protected static final Faker faker = Faker.instance();

    @LocalServerPort
    protected int port;

    @BeforeEach
    void baseSetup() {
        RestAssured.port = port;
    }

    protected String registrarEAutenticar() {
        var email = faker.internet().emailAddress();
        var senha = "Senha#" + faker.number().digits(6);

        given().contentType("application/json")
                .body(Map.of(
                        "name", faker.name().firstName(),
                        "lastname", faker.name().lastName(),
                        "email", email,
                        "password", senha))
                .when().post("/api/v1/register")
                .then().statusCode(201);

        return given().contentType("application/json")
                .body(Map.of("username", email, "password", senha))
                .when().post("/api/v1/authenticate")
                .then().statusCode(200)
                .extract().path("token");
    }
}