package com.github.docker.replica.client.docker.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerErrorResponse {
    private List<DockerError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DockerError {
        private String code;
        private String message;
        private Object detail;
    }
}
