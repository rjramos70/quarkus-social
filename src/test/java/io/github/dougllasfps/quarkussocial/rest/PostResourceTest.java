package io.github.dougllasfps.quarkussocial.rest;

import io.github.dougllasfps.quarkussocial.domain.model.Follower;
import io.github.dougllasfps.quarkussocial.domain.model.Post;
import io.github.dougllasfps.quarkussocial.domain.model.User;
import io.github.dougllasfps.quarkussocial.domain.repository.FollowerRepository;
import io.github.dougllasfps.quarkussocial.domain.repository.PostRepository;
import io.github.dougllasfps.quarkussocial.domain.repository.UserRepository;
import io.github.dougllasfps.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)   // Já pega a URI na anotação @Path da classe passada como parametro
class PostResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUp(){
        // Cria usuário válido
        var user = new User();
        user.setName("Fulano");
        user.setAge(21);
        userRepository.persist(user);
        this.userId = user.getId();

        // Cria uma postagem para o usuário válido
        Post post = new Post();
        post.setUser(user);
        post.setText("Any text");
        postRepository.persist(post);

        // Cria um usuário que não segue ninguém
        var userNotFollower = new User();
        userNotFollower.setName("Beltrano");
        userNotFollower.setAge(23);
        userRepository.persist(userNotFollower);
        this.userNotFollowerId = userNotFollower.getId();

        // Cria usuário Follower (que segue algum outro usuário)
        var userFollower = new User();
        userFollower.setName("Ciclano");
        userFollower.setAge(41);
        userRepository.persist(userFollower);
        this.userFollowerId = userFollower.getId();

        // Cria o vinculo de seguidor entre os usuáriosa travé do objeto Follower
        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);
    }

    @Test
    @DisplayName("should create a post for a user")
    public void createPostTest(){
        // Cria o corpo da requisição
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        // Monta e envia a requisição
        given().contentType(ContentType.JSON)
                    .body(postRequest)
                    .pathParam("userId", this.userId)
                .when()
                    .post()     // Não precisa passar a URL, pois é pego no @TestHTTPEndpoint(PostResource.class)
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("should return 404 when trying to make a post for a non-existent user")
    public void postForAnoExistentUserTest(){
        // Cria o corpo da requisição
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        var noExistentUserId = 999;

        // Monta e envia a requisição
        given().contentType(ContentType.JSON)
                    .body(postRequest)
                    .pathParam("userId", noExistentUserId)
                .when()
                    .post()     // Não precisa passar a URL, pois é pego no @TestHTTPEndpoint(PostResource.class)
                .then()
                    .statusCode(404);
    }

    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest(){
        var inexistentUserId = 999;

        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", inexistentUserId)
                .when()
                    .get()
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("should return 400 when followerId header is not present")
    public void listPostFollowerHeadersNotSendTest(){
        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body(Matchers.is("You forgot to send 'followerId' on the request Header!"));
    }

    @Test
    @DisplayName("should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest(){
        var inexistentFollowerId = 999;

        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                    .header("followerId", inexistentFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body(Matchers.is("Inexistent 'followerId'!"));
    }

    @Test
    @DisplayName("should return 403 when follower isn't a follower")
    public void listPostNotAFollowerTest(){
        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                    .header("followerId", this.userNotFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(403)
                    .body(Matchers.is("You can't see these posts!"));
    }

    @Test
    @DisplayName("should return the list of post")
    public void listPostTest(){
        // Monta e envia a requisição
        given()
                    .contentType(ContentType.JSON)
                    .pathParam("userId", this.userId)
                    .header("followerId", this.userFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(200)
                    .body("size()", Matchers.is(1));
    }
}