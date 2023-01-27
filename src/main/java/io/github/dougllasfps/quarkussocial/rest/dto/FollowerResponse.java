package io.github.dougllasfps.quarkussocial.rest.dto;

import io.github.dougllasfps.quarkussocial.domain.model.Follower;
import lombok.Data;

@Data
public class FollowerResponse {
    private Long followId;
    private Long userId;
    private String name;

    public FollowerResponse() {
    }

    public FollowerResponse(Long followId, Long userId, String name) {
        this.followId = followId;
        this.userId = userId;
        this.name = name;
    }

    // Método estático que transforma de Entidade 'Follower' para 'FollowerResponse'
    public  FollowerResponse (Follower follower){
        this(follower.getId(), follower.getFollower().getId(), follower.getFollower().getName());
    }
}
