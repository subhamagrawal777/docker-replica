package com.github.docker.replica.client.docker.models.response.catalog;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DockerGetCatalogResponse {
    private List<String> repositories;
}
