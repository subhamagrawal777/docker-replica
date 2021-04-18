package com.github.docker.replica.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.docker.replica.exceptions.AppException;
import com.github.docker.replica.exceptions.ErrorCode;
import com.github.docker.replica.utils.models.MapperType;
import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.util.Map;

@UtilityClass
public class MapperUtils {
    private static Map<MapperType, ObjectMapper> objectMapperMap;

    public static void initialize(Map<MapperType, ObjectMapper> objectMapperMap) {
        MapperUtils.objectMapperMap = objectMapperMap;
    }

    public static ObjectMapper getMapper(MapperType mapperType) {
        Preconditions.checkState(objectMapperMap != null && objectMapperMap.containsKey(mapperType), "init not called on MapperUtils");
        return objectMapperMap.get(mapperType);
    }

    public static byte[] serialize(MapperType mapperType, Object data) {
        return serialize(getMapper(mapperType), data);
    }

    public static <T> T deserialize(MapperType mapperType, String data, Class<T> valueType) {
        return deserialize(getMapper(mapperType), data, valueType);
    }

    public static <T> T deserialize(ObjectMapper mapper, InputStream data, TypeReference<T> typeReference) {
        try {
            if (data == null) {
                return null;
            }
            return mapper.readValue(data, typeReference);
        } catch (Exception e) {
            throw AppException.propagate(e, e.getLocalizedMessage(), ErrorCode.DESERIALIZATION_ERROR);
        }
    }

    private static byte[] serialize(ObjectMapper mapper, Object data) {
        try {
            if (data == null) {
                return null;
            }
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw AppException.propagate(e, e.getLocalizedMessage(), ErrorCode.SERIALIZATION_ERROR);
        }
    }

    private static <T> T deserialize(ObjectMapper mapper, String data, Class<T> valueType) {
        try {
            if (data == null) {
                return null;
            }
            return mapper.readValue(data, valueType);
        } catch (Exception e) {
            throw AppException.propagate(e, e.getLocalizedMessage(), ErrorCode.DESERIALIZATION_ERROR);
        }
    }

}
