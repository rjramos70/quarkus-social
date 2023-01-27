package io.github.dougllasfps.quarkussocial.domain.repository;

import io.github.dougllasfps.quarkussocial.domain.model.Post;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped  // Cria uma instância dessa classe dentro do contexto da aplicação
public class PostRepository implements PanacheRepository<Post> {

}
