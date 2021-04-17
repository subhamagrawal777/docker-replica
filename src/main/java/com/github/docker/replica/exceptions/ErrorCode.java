package com.github.docker.replica.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    JSON_ERROR(500, true),
    DAO_ERROR(500, true),
    SQL_CONSTRAINT_EXCEPTION(500, true),
    OPERATION_NOT_SUPPORTED(400, true),
    INTERNAL_SERVER_ERROR(500, true),
    DOCKER_COMMANDS_EXCEPTION(500, true);

    private int httpStatus;
    private boolean stackTraceLoggingEnabled;
}
