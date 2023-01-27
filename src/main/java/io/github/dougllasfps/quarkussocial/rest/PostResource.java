package io.github.dougllasfps.quarkussocial.rest;

import io.github.dougllasfps.quarkussocial.domain.model.Post;
import io.github.dougllasfps.quarkussocial.domain.model.User;
import io.github.dougllasfps.quarkussocial.domain.repository.FollowerRepository;
import io.github.dougllasfps.quarkussocial.domain.repository.PostRepository;
import io.github.dougllasfps.quarkussocial.domain.repository.UserRepository;
import io.github.dougllasfps.quarkussocial.rest.dto.CreatePostRequest;
import io.github.dougllasfps.quarkussocial.rest.dto.CreateUserRequest;
import io.github.dougllasfps.quarkussocial.rest.dto.PostResponse;
import io.github.dougllasfps.quarkussocial.rest.dto.ResponseError;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import org.jboss.logging.annotations.Pos;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")          // Para começar a receber requisições REST HTTP
@Consumes(MediaType.APPLICATION_JSON)   // Diz qual tipo de dados vamos receber nas requisições (Request)
@Produces(MediaType.APPLICATION_JSON)   // Diz qual tipo de dados vamos produzir para as respostas (Response)
public class PostResource {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;
    private Validator validator;

    @Inject
    public PostResource(
            UserRepository userRepository,
            PostRepository postRepository,
            FollowerRepository followerRepository,
            Validator validator){
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
        this.validator = validator;
    }

    @POST
    @Transactional
    public Response savePost(
            @PathParam("userId") Long userId,
            CreatePostRequest postRequest){
        // Valida o corpo da requisição
        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(postRequest);
        if (!violations.isEmpty()){
            return ResponseError
                    .createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }
        // Pesquisa se o usuário existe na base de dados
        User user = userRepository.findById(userId);
        if (user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Cria nova postagem para o userId recebido
        Post post = new Post();
        post.setText(postRequest.getText());
        post.setUser(user);

        // Salva a postagem
        postRepository.persist(post);

        return Response
                .status(Response.Status.CREATED.getStatusCode())
                .entity(post)
                .build();
    }

    @GET
    public Response listPosts(
            @PathParam("userId") Long userId,
            @HeaderParam("followerId") Long followerId){
        User user = userRepository.findById(userId);
        if (user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Verifica se o atributo 'followerId' foi enviado no Headers da requisição
        if (followerId == null){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("You forgot to send 'followerId' on the request Header!")
                    .build();
        }

        // Depois de validar seo 'userId' existe, devemos validar se o
        // 'followerId' é um usuário que tem permissão de listas as
        // postagem desse 'userId'
        // 1. Busca o Follower na base
        var follower = userRepository.findById(followerId);
        // Se Follwer não existir
        if (follower == null){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Inexistent 'followerId'!")
                    .build();
        }

        // 2. Verificar se existe alguma relação entre 'follower' e 'user'
        boolean follows = followerRepository.follows(follower, user);

        // Se não seguir, retornar o status que esse seguir não tem permissão '403' FORBIDDEN
        // significa que o usuário pode até estar autenticado, porém pela regra de negócio ele
        // esta proibido de acessar esse recurso.
        if (!follows){
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("You can't see these posts!")
                    .build();
        }

        // O Panache filtra por qualquer que existe dentro de 'POST'
        // ai passamos o nome do atributo 'user' e o respectivo objeto 'User'
        PanacheQuery<Post> posts = postRepository
                .find("user", Sort.by("dateTime", Sort.Direction.Descending), user);
        var list = posts.list();

        // Transformar a lista de 'Post' em lista de 'PostResponse'
        List<PostResponse> postResponseList = list
                .stream()
        //        .map(p -> PostResponse.fromEntity(p))
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());

        return Response.ok(postResponseList).build();
    }

}
