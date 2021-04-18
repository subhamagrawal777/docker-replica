package com.github.docker.replica.client.docker;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.docker.replica.client.docker.models.DockerErrorCodeMap;
import com.github.docker.replica.exceptions.AppException;
import com.github.docker.replica.exceptions.ErrorCode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.phonepe.platform.http.v2.discovery.ServiceEndpointProviderFactory;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class DockerCommandsFactory {
    private Map<DockerSubDomain, DockerCommands> dockerCommandsProviderMap;

    @Inject
    public DockerCommandsFactory(final ServiceEndpointProviderFactory serviceEndpointProviderFactory,
                                 final Environment environment,
                                 final ObjectMapper objectMapper,
                                 final MetricRegistry metricRegistry,
                                 final Map<DockerSubDomain, HttpConfiguration> httpConfigurationMap,
                                 @DockerErrorCodeMap final Map<String, ErrorCode> errorCodeMap) {
        dockerCommandsProviderMap = Arrays.stream(DockerSubDomain.values())
                .filter(httpConfigurationMap::containsKey)
                .collect(Collectors.toMap(
                        Function.identity(),
                        dockerSubDomain -> {
                            try {
                                return new DockerCommands(httpConfigurationMap.get(dockerSubDomain),
                                        serviceEndpointProviderFactory, environment, objectMapper, metricRegistry, errorCodeMap);
                            } catch (GeneralSecurityException | IOException e) {
                                log.error("Error initializing Docker Commands for {}", dockerSubDomain, e);
                                throw AppException.propagate(e, ErrorCode.INTERNAL_SERVER_ERROR);
                            }
                        }));
    }

    public DockerCommands get(DockerSubDomain dockerSubDomain) {
        return dockerCommandsProviderMap.get(dockerSubDomain);
    }
}
