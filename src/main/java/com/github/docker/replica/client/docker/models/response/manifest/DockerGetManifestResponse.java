package com.github.docker.replica.client.docker.models.response.manifest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.docker.replica.client.docker.models.DockerLayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerGetManifestResponse {
    private String schemaVersion;

    @JsonProperty("name")
    private String imageName;

    private String tag;

    private String architecture;

    @JsonProperty("fsLayers")
    private List<DockerLayer> layers;

    private Object history;

    private Object signatures;
}
