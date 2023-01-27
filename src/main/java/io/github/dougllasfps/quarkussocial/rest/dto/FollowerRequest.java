package io.github.dougllasfps.quarkussocial.rest.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FollowerRequest {

    @NotNull(message = "FollowerID is required")
    private Long followerId;

}
