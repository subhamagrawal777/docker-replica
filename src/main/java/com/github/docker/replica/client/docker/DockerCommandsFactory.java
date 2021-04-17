package com.github.docker.replica.client.docker;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.docker.replica.exceptions.AppException;
import com.github.docker.replica.exceptions.ErrorCode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

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
    public DockerCommandsFactory(final ObjectMapper objectMapper,
                                 final CuratorFramework curatorFramework,
                                 final MetricRegistry metricRegistry,
                                 final Map<DockerSubDomain, HttpConfiguration> httpConfigurationMap) {
        dockerCommandsProviderMap = Arrays.stream(DockerSubDomain.values())
                .filter(httpConfigurationMap::containsKey)
                .collect(Collectors.toMap(
                        Function.identity(),
                        dockerSubDomain -> {
                            try {
                                return new DockerCommands(
                                        new DockerCommandsProvider(httpConfigurationMap.get(dockerSubDomain),
                                                objectMapper, curatorFramework),
                                        objectMapper, metricRegistry);
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
