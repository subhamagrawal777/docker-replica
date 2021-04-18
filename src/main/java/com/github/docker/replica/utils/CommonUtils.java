package com.github.docker.replica.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.docker.replica.exceptions.AppException;
import com.github.docker.replica.exceptions.ErrorCode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@UtilityClass
public class CommonUtils {
    public static <T, R> R getResource(ObjectMapper mapper, String filePath, Class<T> sourceClass, TypeReference<R> typeReference) {
        try (InputStream inputstream = sourceClass.getClassLoader().getResourceAsStream(filePath)) {
            return MapperUtils.deserialize(mapper, inputstream, typeReference);
        } catch (IOException e) {
            log.error("There is a problem trying to load resource from :" + filePath, e);
            throw AppException.propagate(e, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
