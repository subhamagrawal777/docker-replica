package com.github.docker.replica.exceptions;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> context;

    @Builder
    public AppException(ErrorCode errorCode, String message, Throwable throwable, Map<String, Object> context) {
        super(message, throwable);
        this.errorCode = errorCode;
        this.context = context;
    }


    public static AppException propagate(Throwable e) {
        return propagate(e, e.getLocalizedMessage(), ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public static AppException propagate(Throwable e, ErrorCode errorCode) {
        return propagate(e, e.getLocalizedMessage(), errorCode);
    }

    public static AppException propagate(Throwable e,
                                         String message,
                                         ErrorCode errorCode) {
        AppException appException = connectorException(e);
        if (appException != null && appException.getErrorCode() != null) {
            return appException;
        }
        return AppException.builder()
                .errorCode(errorCode)
                .message(message)
                .throwable(e)
                .build();
    }

    private static AppException connectorException(Throwable e) {
        if (e == null) {
            return null;
        }
        if (e instanceof AppException) {
            return (AppException) e;
        }

        //Only going one level not till root cause as it is an expensive operation
        if (e.getCause() instanceof AppException) {
            return (AppException) e.getCause();
        }
        return null;
    }
}
