package io.github.dougllasfps.quarkussocial.domain.repository;

import io.github.dougllasfps.quarkussocial.domain.model.Follower;
import io.github.dougllasfps.quarkussocial.domain.model.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped  // Cria uma instância dessa classe dentro do contexto da aplicação com todas as suas injeções de dependência já necessárias
public class FollowerRepository implements PanacheRepository<Follower> {

    public boolean follows(User follower, User user){
        // Cria um mapa dos parametros que serão passado no find()
        // Map<String, Object> params = new HashMap<>();
        // params.put("follower", follower);
        // params.put("user", user);

        // Ou pode-se criar o Map usando o Parameters do Panache conforme abaixo
        Map<String, Object> params = Parameters
                .with("follower", follower)
                .and("user", user)
                .map();

        PanacheQuery<Follower> query = find("follower = :follower and user = :user", params);

        // Como query é uma lista, pegar e verificar o primeiro item da lista
        Optional<Follower> result = query.firstResultOptional();

        // Como é um Optional, só verificar se existe algo presente ou não
        // SE presente é porque o 'follower' já segue o respectivo 'user'
        return result.isPresent();
    }

    public List<Follower> findByUser(Long userId){
        var query = find("user.id", userId);
        return query.list();
    }

    public void deleteByFollowerAndUser(Long followerId, Long userId){
        // Criar o Map usando o Parameters do Panache conforme abaixo
        Map<String, Object> params = Parameters
                .with("userId", userId)
                .and("followerId", followerId)
                .map();

        // Remove do banco de dados usando o método 'find()' herdado do Panache
        delete("follower.id = :followerId and user.id =: userId", params);
    }
}
