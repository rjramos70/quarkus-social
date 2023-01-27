package io.github.dougllasfps.quarkussocial.rest;

import io.github.dougllasfps.quarkussocial.domain.model.User;
import io.github.dougllasfps.quarkussocial.domain.repository.UserRepository;
import io.github.dougllasfps.quarkussocial.rest.dto.CreateUserRequest;
import io.github.dougllasfps.quarkussocial.rest.dto.ResponseError;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/users")                         // Para começar a receber requisições REST HTTP
@Consumes(MediaType.APPLICATION_JSON)   // Diz qual tipo de dados vamos receber nas requisições (Request)
@Produces(MediaType.APPLICATION_JSON)   // Diz qual tipo de dados vamos produzir para as respostas (Response)
public class UserResource {

    private UserRepository repository;
    private Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator){
        this.repository = repository;
        this.validator = validator;
    }

    @POST
    @Transactional  // Abre uma transação com o banco de dados
    public Response createUser(CreateUserRequest userRequest){
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);
        if (!violations.isEmpty()){
            return ResponseError
                    .createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }
        // Cria um objeto User vazio
        User user = new User();
        // Seta os atributos
        user.setName(userRequest.getName());
        user.setAge(userRequest.getAge());

        // Persiste o objeto 'user' no banco de dados
        repository.persist(user);

        return Response
                .status(Response.Status.CREATED.getStatusCode())
                .entity(user)
                .build();
    }

    @GET
    public Response listAllUsers(){
        PanacheQuery<User> query = repository.findAll();
        return Response.ok(query.list()).build();
    }

    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") Long id){
        User user = repository.findById(id);
        // Verifica se o User existe
        if (user != null){
            // Retorna o 'user'
            return Response.ok(user).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest userData){
        User user = repository.findById(id);
        // Verifica se o User existe
        if (user != null){
            // Atualiza o 'user' com os dados do 'userData'
            user.setName(userData.getName());
            user.setAge(userData.getAge());
            // Salva as atualizações
            // pelo fato de o método estar anotado como Transactional, toda alteração que for feita
            // na entidade dentro do método será salva no banco de dados quando finalizado o método.
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Transactional
    @Path("{id}")
    public Response deleteUser(@PathParam("id") Long id){
        User user = repository.findById(id);
        // Verifica se o User existe
        if (user != null){
            // Usa o método para remover
            repository.delete(user);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
