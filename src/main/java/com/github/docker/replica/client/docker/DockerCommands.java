package com.github.docker.replica.client.docker;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.docker.replica.client.docker.models.response.DockerErrorResponse;
import com.github.docker.replica.client.docker.models.response.catalog.DockerGetCatalogResponse;
import com.github.docker.replica.client.docker.models.response.manifest.DockerGetManifestResponse;
import com.github.docker.replica.client.docker.models.response.tag.DockerGetTagsResponse;
import com.github.docker.replica.exceptions.AppException;
import com.github.docker.replica.exceptions.ErrorCode;
import com.github.docker.replica.utils.MapperUtils;
import com.github.docker.replica.utils.models.MapperType;
import com.google.common.base.Strings;
import com.phonepe.platform.http.v2.client.ClientFactory;
import com.phonepe.platform.http.v2.common.Endpoint;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.phonepe.platform.http.v2.common.ServiceEndpointProvider;
import com.phonepe.platform.http.v2.discovery.ServiceEndpointProviderFactory;
import com.phonepe.platform.http.v2.executor.Consumer;
import com.phonepe.platform.http.v2.executor.ExtractedResponse;
import com.phonepe.platform.http.v2.executor.HttpGetExecutor;
import io.appform.core.hystrix.CommandFactory;
import io.appform.functionmetrics.MonitoredFunction;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class DockerCommands {

    private final HttpConfiguration httpConfiguration;
    private final ObjectMapper objectMapper;
    private final ServiceEndpointProvider serviceEndpointProvider;
    private final OkHttpClient client;
    private final Map<String, ErrorCode> errorCodeMap;

    public DockerCommands(final HttpConfiguration httpConfiguration,
                          final ServiceEndpointProviderFactory serviceEndpointProviderFactory,
                          final Environment environment,
                          final ObjectMapper objectMapper,
                          final MetricRegistry metricRegistry,
                          final Map<String, ErrorCode> errorCodeMap) throws GeneralSecurityException, IOException {
        this.httpConfiguration = httpConfiguration;
        this.objectMapper = objectMapper;
        this.errorCodeMap = errorCodeMap;
        this.serviceEndpointProvider = serviceEndpointProviderFactory.provider(httpConfiguration, environment);
        this.client = new ClientFactory.HttpClientBuilder()
                .withConfiguration(httpConfiguration)
                .withMetricRegistry(metricRegistry)
                .build();
    }

    @MonitoredFunction
    public DockerGetCatalogResponse getCatalog(int limit, String last) {
        try {
            return HttpGetExecutor.<DockerGetCatalogResponse>builder()
                    .url(String.format("/v2/_catalog?n=%d&last=%s", limit, last))
                    .client(this.client)
                    .mapper(this.objectMapper)
                    .endpointProvider(serviceEndpointProvider)
                    .command("getCatalog")
                    .responseType(DockerGetCatalogResponse.class)
                    .nonSuccessResponseConsumer(this.handleNonSuccessCall("Error while calling GetCatalog"))
                    .build()
                    .executeTracked(DockerCommands.class);
        } catch (Exception e) {
            throw AppException.propagate(e, ErrorCode.DOCKER_COMMANDS_EXCEPTION);
        }
    }

    @MonitoredFunction
    public DockerGetTagsResponse getTags(String imageName, int limit, String last) {
        try {
            return HttpGetExecutor.<DockerGetTagsResponse>builder()
                    .url(String.format("/v2/%s/tags/list?n=%d&last=%s", imageName, limit, last))
                    .client(this.client)
                    .mapper(this.objectMapper)
                    .endpointProvider(serviceEndpointProvider)
                    .command("getCatalog")
                    .responseType(DockerGetTagsResponse.class)
                    .nonSuccessResponseConsumer(this.handleNonSuccessCall(String.format("Error while Calling GetTags for %s", imageName)))
                    .build()
                    .executeTracked(DockerCommands.class);
        } catch (Exception e) {
            throw AppException.propagate(e, ErrorCode.DOCKER_COMMANDS_EXCEPTION);
        }
    }

    @MonitoredFunction
    public DockerGetManifestResponse getManifest(String imageName, String tagName) {
        try {
            return HttpGetExecutor.<DockerGetManifestResponse>builder()
                    .url(String.format("/v2/%s/manifests/%s", imageName, tagName))
                    .client(this.client)
                    .mapper(this.objectMapper)
                    .endpointProvider(serviceEndpointProvider)
                    .command("getCatalog")
                    .responseType(DockerGetManifestResponse.class)
                    .nonSuccessResponseConsumer(this.handleNonSuccessCall(String.format("Error while Calling GetManifest for %s", imageName)))
                    .build()
                    .executeTracked(DockerCommands.class);
        } catch (Exception e) {
            throw AppException.propagate(e, ErrorCode.DOCKER_COMMANDS_EXCEPTION);
        }
    }

    @MonitoredFunction
    public boolean manifestExists(String imageName, String tagName) {
        try {
            final String httpUrl = String.format("/v2/%s/manifests/%s", imageName, tagName);
            Response response = execute(httpUrl, "manifestExists");

            String eTag = response.header("ETag");
            return !Strings.isNullOrEmpty(eTag);
        } catch (Exception e) {
            throw AppException.propagate(e, ErrorCode.DOCKER_COMMANDS_EXCEPTION);
        }
    }

    @MonitoredFunction
    public boolean blobExists(String imageName, String blobSha) {
        try {
            val httpUrl = String.format("/v2/%s/blobs/%s", imageName, blobSha);
            Response response = execute(httpUrl, "blobExists");

            String eTag = response.header("ETag");
            return !Strings.isNullOrEmpty(eTag);
        } catch (Exception e) {
            throw AppException.propagate(e, ErrorCode.DOCKER_COMMANDS_EXCEPTION);
        }
    }

    @MonitoredFunction
    public InputStream downloadBlob(String imageName, String blobSha) {
        val httpUrl = String.format("/v2/%s/blobs/%s", imageName, blobSha);
        try {
            Request request = new Request.Builder()
                    .url(this.buildHttpUrl(httpUrl))
                    .build();

            return CommandFactory.<InputStream>create(DockerCommands.class.getSimpleName(), "downloadBlob")
                    .executor(() -> {
                        Response response = client.newCall(request).execute();

                        if (!response.isSuccessful()) {
                            throw AppException.builder()
                                    .errorCode(ErrorCode.DOCKER_COMMANDS_EXCEPTION)
                                    .message(String.format("Error downloading blob from docker registry for the imageName %s, blobSha %s",
                                            imageName, blobSha))
                                    .build();
                        } else {
                            return response.body().byteStream();
                        }
                    })
                    .execute();

        } catch (Exception e) {
            throw AppException.propagate(e, ErrorCode.DOCKER_COMMANDS_EXCEPTION);
        }
    }

    private <T> Consumer<ExtractedResponse, T> handleNonSuccessCall(String message) {
        return extractedResponse -> {
            int code = extractedResponse.getCode();
            byte[] body = extractedResponse.getBody();
            String bodyString = new String(body);
            val errorResponse = MapperUtils.deserialize(MapperType.JSON, bodyString, DockerErrorResponse.class);
            log.error(message + " code:{}, response:{}", code, bodyString);
            throw AppException.builder()
                    .errorCode(ErrorCode.DOCKER_COMMANDS_EXCEPTION)
                    .message(message)
                    .build();
        };
    }

    private Response execute(final String httpUrl, final String commandName) {
        Request request = new Request.Builder()
                .url(buildHttpUrl(httpUrl))
                .head()
                .build();

        log.info("Calling Docker Registry for {} and request is {}", commandName, request); //Body won't be logged in this
        return CommandFactory.<Response>create(DockerCommands.class.getSimpleName(), commandName)
                .executor(() -> client.newCall(request).execute())
                .execute();
    }

    private HttpUrl buildHttpUrl(final String url) {
        Optional<Endpoint> endpointOptional = serviceEndpointProvider.endpoint();
        if (!endpointOptional.isPresent()) {
            throw AppException.builder()
                    .message("Client endpoints have not been configured")
                    .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                    .build();
        }
        return httpConfiguration.isSecure()
                ? endpointOptional.get().secureUrl(url)
                : endpointOptional.get().url(url);
    }

}
