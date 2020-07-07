package ssotom.clone.reddit.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ssotom.clone.reddit.demo.model.Subreddit;
import ssotom.clone.reddit.demo.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubredditDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private String description;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int postCount;

    public Subreddit mapToEntity(User user) {
        return Subreddit.builder()
                .name(name)
                .description(description)
                .posts(Collections.emptyList())
                .user(user)
                .build();
    }

}
