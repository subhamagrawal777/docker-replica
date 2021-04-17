package com.github.docker.replica.client.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import org.apache.curator.framework.CuratorFramework;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class DockerCommandsProviderFactory {

    private Map<DockerSubDomain, DockerCommandsProvider> dockerCommandsProviderMap;

    public DockerCommandsProviderFactory(final ObjectMapper objectMapper,
                                         final CuratorFramework curatorFramework,
                                         final Map<DockerSubDomain, HttpConfiguration> httpConfigurationMap) {
        dockerCommandsProviderMap = Arrays.stream(DockerSubDomain.values())
                .filter(httpConfigurationMap::containsKey)
                .collect(Collectors.toMap(
                        Function.identity(),
                        dockerSubDomain -> new DockerCommandsProvider(httpConfigurationMap.get(dockerSubDomain),
                                objectMapper, curatorFramework)));
    }

    public DockerCommandsProvider get(DockerSubDomain dockerSubDomain) {
        return dockerCommandsProviderMap.get(dockerSubDomain);
    }
}
