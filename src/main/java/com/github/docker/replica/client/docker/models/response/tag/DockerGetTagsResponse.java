package com.github.docker.replica.client.docker.models.response.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DockerGetTagsResponse {
    @JsonProperty("name")
    private String imageName;

    private List<String> tags;
}
