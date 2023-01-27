package io.github.dougllasfps.quarkussocial.rest.dto;

import io.github.dougllasfps.quarkussocial.domain.model.Post;
import lombok.Data;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
public class PostResponse {

    private String text;
    private LocalDateTime dateTime;

    public static PostResponse fromEntity(Post post){
        var postResponse = new PostResponse();
        postResponse.setText(post.getText());
        postResponse.setDateTime(post.getDateTime());
        return postResponse;
    }
}
