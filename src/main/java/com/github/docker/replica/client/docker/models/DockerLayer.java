package com.github.docker.replica.client.docker.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DockerLayer {
    private String blobSum;
}
