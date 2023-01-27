package io.github.dougllasfps.quarkussocial.rest;

import io.github.dougllasfps.quarkussocial.domain.model.Follower;
import io.github.dougllasfps.quarkussocial.domain.repository.FollowerRepository;
import io.github.dougllasfps.quarkussocial.domain.repository.UserRepository;
import io.github.dougllasfps.quarkussocial.rest.dto.*;

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

@Path("/users/{userId}/followers")      // Para começar a receber requisições REST HTTP
@Consumes(MediaType.APPLICATION_JSON)   // Diz qual tipo de dados vamos receber nas requisições (Request)
@Produces(MediaType.APPLICATION_JSON)   // Diz qual tipo de dados vamos produzir para as respostas (Response)
public class FollowerResource {

    private FollowerRepository followerRepository;
    private UserRepository userRepository;
    private Validator validator;

    @Inject
    public FollowerResource(
            FollowerRepository followerRepository,
            UserRepository userRepository,
            Validator validator){
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @PUT
    @Transactional
    public Response followUser(
            @PathParam("userId") Long userId,
            FollowerRequest followerRequest){
        // Valida o corpo da requisição
        Set<ConstraintViolation<FollowerRequest>> violations = validator.validate(followerRequest);
        if (!violations.isEmpty()){
            return ResponseError
                    .createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }
        // Verifica se o follower e o user são os mesmos, pois um usuário não pode seguir a si próprio.
        if (userId.equals(followerRequest.getFollowerId())){
            // Retorna o status 409 que é um conflito na regra de negócio
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("You can't follow yourself!")
                    .build();
        }


        // Pesquisa se o usuário existe na base de dados
        var user = userRepository.findById(userId);
        if (user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Pesquisa se o follower existe na base de dados
        var follower = userRepository.findById(followerRequest.getFollowerId());
        if (follower == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Se 'follower' não esta seguindo este 'user'
        if (!followerRepository.follows(follower, user)){
            // Cria a entidade 'Follower' que será salva no banco de dados
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);

            // Salvar a entidade no banco de dados
            followerRepository.persist(entity);
        }

        // Retorna o status NO CONTENT seguindo ou não o respectivo User
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId){
        // Pesquisa se o usuário existe na base de dados
        var user = userRepository.findById(userId);
        if (user == null){
            return Response.status(Response.Status.NOT_FOUND).entity("This userId do not exist!").build();
        }

        // Busca os followers para o respectivo 'userId'
        List<Follower> list = followerRepository.findByUser(userId);

        // Cria o objeto de retorno 'FollowersPerUserResponse'
        FollowersPerUserResponse responseObject = new FollowersPerUserResponse();
        // Seta quandidade de followers
        responseObject.setFollowersCount(list.size());

        // Popula o atributo 'content' que é uma lista de 'FollowerResponse'
        List<FollowerResponse> followerList = list.stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());

        // Seta lista de follower no objeto 'FollowersPerUserResponse'
        responseObject.setContent(followerList);

        // retorna o objeto 'FollowersPerUserResponse'
        return Response.ok(responseObject).build();

    }

    @DELETE
    @Transactional
    public Response unfollowUser(
            @PathParam("userId") Long userId,
            @QueryParam("followerId") Long followerId){
        // Pesquisa se o usuário existe na base de dados
        var user = userRepository.findById(userId);
        if (user == null){
            return Response.status(Response.Status.NOT_FOUND).entity("This userId do not exist!").build();
        }

        // Utiliza o método 'deleteByFollowerAndUser' da classe 'FollowerRepository' para
        // remover a relação entre seguidor e usuário
        followerRepository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
