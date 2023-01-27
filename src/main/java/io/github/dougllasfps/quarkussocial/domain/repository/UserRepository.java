package io.github.dougllasfps.quarkussocial.domain.repository;

import io.github.dougllasfps.quarkussocial.domain.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped     // Cria uma instância dessa classe dentro do contexto da aplicação com todas as suas injeções de dependência já necessárias
public class UserRepository implements PanacheRepository<User> {

}
