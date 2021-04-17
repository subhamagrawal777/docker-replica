package com.github.docker.replica.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericResponse<T> {
    private boolean success;
    private T data;
    private String code;
    private String message;
}
