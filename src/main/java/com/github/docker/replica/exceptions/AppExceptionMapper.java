package com.github.docker.replica.exceptions;

import com.github.docker.replica.models.GenericResponse;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;

@Slf4j
public class AppExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<AppException> {

    @Override
    public Response toResponse(AppException exception) {
        if (exception.getErrorCode().isStackTraceLoggingEnabled()) {
            log.error("ERROR Message: {}, context: {}", exception.getMessage(), exception.getContext(), exception);
        } else {
            log.error("ERROR Message: {}, context: {}", exception.getMessage(), exception.getContext());
        }
        return Response.status(exception.getErrorCode().getHttpStatus())
                .entity(GenericResponse.builder()
                        .success(false)
                        .code(exception.getErrorCode().name())
                        .data(exception.getContext())
                        .build())
                .build();
    }
}
