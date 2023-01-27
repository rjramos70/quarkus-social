package io.github.dougllasfps.quarkussocial.rest;

import io.github.dougllasfps.quarkussocial.rest.dto.CreateUserRequest;
import io.github.dougllasfps.quarkussocial.rest.dto.ResponseError;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    @TestHTTPResource("/users")
    URL apiURL;

    @Test
    @DisplayName("should create an user sucessfully")
    @Order(1)
    public void createUserTest(){
        // Monta o corpo da requisição
        var user = new CreateUserRequest();
        user.setName("Fulano");
        user.setAge(52);

        // Monta e envia a requisição
        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(user)
                .when()
                    .post(apiURL)
                .then()
                    .extract()
                    .response();

        // Quando eu crio o processo com sucesso
        assertEquals(201, response.statusCode());
        assertNotNull(response.jsonPath().getString("id"));

    }

    @Test
    @DisplayName("should return error when json is not valid")
    @Order(2)
    public void createUserValidationErrorTest(){
        // Monta o corpo da requisição com um User e seus atributos como NULL
        var user = new CreateUserRequest();
        user.setName(null);
        user.setAge(null);

        // Monta e envia a requisição
        var response =
                given()
                            .contentType(ContentType.JSON)
                            .body(user)
                        .when()
                            .post(apiURL)
                        .then()
                            .extract()
                            .response();

        // Quando eu crio o processo com sucesso
        assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
        assertEquals("Validation Error", response.jsonPath().getString("message"));

        // Testar os retornos da lista de erros
        // Obtem a lista de erros no response
        List<Map<String, String>> errors = response.jsonPath().getList("errors");
        // Testa elemento da lista
        assertNotNull(errors.get(0).get("message"));
        assertNotNull(errors.get(1).get("message"));
//        assertEquals("Age is required", errors.get(0).get("message"));
//        assertEquals("Name is required", errors.get(1).get("message"));
    }

    @Test
    @DisplayName("should list all users")
    @Order(3)
    public void listAllUsersTest(){
        // Monta e envia a requisição
        given()
                .contentType(ContentType.JSON)
                .when()
                    .get(apiURL)
                .then()
                    .statusCode(200)    // Testa se status de retorno é 200
                    .body("size()", Matchers.is(1));    // Testa se o tamanho do array de itens tem 1 elemento
    }

}