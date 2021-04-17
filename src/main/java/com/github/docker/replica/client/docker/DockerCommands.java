package com.github.docker.replica.client.docker;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.docker.replica.client.docker.models.response.catalog.DockerGetCatalogResponse;
import com.github.docker.replica.client.docker.models.response.tag.DockerGetTagsResponse;
import com.github.docker.replica.exceptions.AppException;
import com.github.docker.replica.exceptions.ErrorCode;
import com.phonepe.platform.http.v2.client.ClientFactory;
import com.phonepe.platform.http.v2.executor.Consumer;
import com.phonepe.platform.http.v2.executor.ExtractedResponse;
import com.phonepe.platform.http.v2.executor.HttpGetExecutor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
public class DockerCommands {
//    private final DockerCommandsProviderFactory dockerCommandsProviderFactory;

    private final DockerCommandsProvider dockerCommandsProvider;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;


    public DockerCommands(final DockerCommandsProvider dockerCommandsProvider,
                          final ObjectMapper objectMapper,
                          final MetricRegistry metricRegistry) throws GeneralSecurityException, IOException {
        this.dockerCommandsProvider = dockerCommandsProvider;
        this.objectMapper = objectMapper;
        this.client = new ClientFactory.HttpClientBuilder()
                .withConfiguration(dockerCommandsProvider.getHttpConfiguration())
                .withMetricRegistry(metricRegistry)
                .build();
    }

    public DockerGetCatalogResponse getCatalog(int limit, String last) {
        try {
            return HttpGetExecutor.<DockerGetCatalogResponse>builder()
                    .url(String.format("/v2/_catalog?n=%d&last=%s", limit, last))
                    .client(this.client)
                    .mapper(this.objectMapper)
                    .endpointProvider(dockerCommandsProvider.getServiceEndpointProvider())
                    .command("getCatalog")
                    .responseType(DockerGetCatalogResponse.class)
                    .nonSuccessResponseConsumer(this.handleNonSuccessCall("Error while calling GetCatalog"))
                    .build()
                    .executeTracked(DockerCommands.class);
        } catch (Exception e) {
            throw AppException.propagate(e, ErrorCode.DOCKER_COMMANDS_EXCEPTION);
        }
    }

    public DockerGetTagsResponse getTags(String imageName, int limit, String last) {
        try {
            return HttpGetExecutor.<DockerGetTagsResponse>builder()
                    .url(String.format("/v2/%s/tags/list?n=%d&last=%s", imageName, limit, last))
                    .client(this.client)
                    .mapper(this.objectMapper)
                    .endpointProvider(dockerCommandsProvider.getServiceEndpointProvider())
                    .command("getCatalog")
                    .responseType(DockerGetTagsResponse.class)
                    .nonSuccessResponseConsumer(this.handleNonSuccessCall(String.format("Error while Calling GetTags for %s", imageName)))
                    .build()
                    .executeTracked(DockerCommands.class);
        } catch (Exception e) {
            throw AppException.propagate(e, ErrorCode.DOCKER_COMMANDS_EXCEPTION);
        }
    }

    private <T> Consumer<ExtractedResponse, T> handleNonSuccessCall(String message) {
        return extractedResponse -> {
            int code = extractedResponse.getCode();
            byte[] body = extractedResponse.getBody();
            String errorResponse = new String(body);
            log.error(message + " code:{}, response:{}", code, errorResponse);
            throw AppException.builder()
                    .errorCode(ErrorCode.DOCKER_COMMANDS_EXCEPTION)
                    .message(message)
                    .build();
        };
    }

}
