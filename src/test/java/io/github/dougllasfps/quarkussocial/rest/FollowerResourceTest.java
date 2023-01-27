package io.github.dougllasfps.quarkussocial.rest;

import io.github.dougllasfps.quarkussocial.domain.model.Follower;
import io.github.dougllasfps.quarkussocial.domain.model.User;
import io.github.dougllasfps.quarkussocial.domain.repository.FollowerRepository;
import io.github.dougllasfps.quarkussocial.domain.repository.UserRepository;
import io.github.dougllasfps.quarkussocial.rest.dto.FollowerRequest;
import io.github.dougllasfps.quarkussocial.rest.dto.ResponseError;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    private FollowerRepository followerRepository;
    @Inject
    private UserRepository userRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    public void setUp(){
        // Cria usuário válido
        var user = new User();
        user.setName("Fulano");
        user.setAge(21);
        userRepository.persist(user);
        this.userId = user.getId();

        // Cria usuário seguidor
        var followerUser = new User();
        followerUser.setName("Beltrano");
        followerUser.setAge(33);
        userRepository.persist(followerUser);
        this.followerId = followerUser.getId();

        // Cria a relação de seguidor a cada execução
        var follower = new Follower();
        follower.setUser(user);
        follower.setFollower(followerUser);
        followerRepository.persist(follower);

    }

    @Test
    @DisplayName("should return 409 when Follower Id is equal to User id")
    public void sameUserAsFollowerTest(){
        var followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(this.userId);

        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                    .body(followerRequest)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.CONFLICT.getStatusCode())    // Status 409
                    .body(Matchers.is("You can't follow yourself!"));

    }

    @Test
    @DisplayName("should return 404 on follow an user when User id doesn't exist")
    public void userNotFoundWhenTryingToFollowTest(){
        var inexistentUserId = 999;

        var followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(this.userId);

        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", inexistentUserId)
                    .body(followerRequest)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());    // Status 404
    }

    @Test
    @DisplayName("should return 404 when Follower id doesn't exist")
    public void followerNotFoundTest(){

        var followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(999L);

        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                    .body(followerRequest)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());    // Status 404
    }

    @Test
    @DisplayName("should return 402 when the body attribute is incorrect")
    public void bodyIsIncorrectTest(){
        var followerRequest = new FollowerRequest();
        // followerRequest.setFollowerId(this.followerId);

        // Monta e envia a requisição
        var response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                    .body(followerRequest)
                .when()
                    .put()
                .then()
                    .extract()
                    .response();


        assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
        // Esperado um campo na lista de atributos não enviados no body
        assertEquals(1, response.jsonPath().getList("errors").size());
        assertEquals("Validation Error", response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("should follow a user")
    public void followUserTest(){
        var followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(this.followerId);

        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                    .body(followerRequest)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.NO_CONTENT.getStatusCode());    // Status 404
    }

    @Test
    @DisplayName("should return 404 on list user followers and User id doesn't exist")
    public void userNotFoundWhenListingFollowersTest(){
        var inexistentUserId = 999;

        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", inexistentUserId)
                .when()
                    .get()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());    // Status 404
    }

    @Test
    @DisplayName("should list an user's followers")
    public void listFollowersTest(){
        // Monta e envia a requisição
        var response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                .when()
                    .get()
                .then()
                    .extract()
                    .response();

        // Quantidade de followers de um determinado User
        var followersCount = response.jsonPath().get("followersCount");
        // Obtem a lista dos seguidores
        var followersContext = response.jsonPath().getList("content");

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContext.size());
    }

    @Test
    @DisplayName("should return 404 on unfollow user  and User id doesn't exist")
    public void userNotFoundWhenUnfollowingAUserTest(){
        var inexistentUserId = 999;

        // Monta e envia a requisição
        given()
                    .pathParam("userId", inexistentUserId)
                    .queryParam("followerId", this.followerId)
                .when()
                    .delete()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());    // Status 404
    }

    @Test
    @DisplayName("should unfollow an User")
    public void unfollowUserTest(){
        // Monta e envia a requisição
        given()
                    .pathParam("userId", this.userId)
                    .queryParam("followerId", this.followerId)
                .when()
                    .delete()
                .then()
                    .statusCode(Response.Status.NO_CONTENT.getStatusCode());    // Status 204
    }
}