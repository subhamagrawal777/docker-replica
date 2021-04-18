package com.github.docker.replica;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.docker.replica.actor.ActorConfiguration;
import com.github.docker.replica.client.docker.DockerSubDomain;
import com.github.docker.replica.client.docker.models.DockerErrorCodeMap;
import com.github.docker.replica.exceptions.ErrorCode;
import com.github.docker.replica.utils.CommonUtils;
import com.github.docker.replica.utils.MapperUtils;
import com.github.docker.replica.utils.models.MapperType;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.phonepe.platform.http.v2.discovery.ServiceEndpointProviderFactory;
import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryBundle;
import io.github.qtrouper.TrouperBundle;
import io.github.qtrouper.core.rabbit.RabbitConnection;
import lombok.AllArgsConstructor;
import org.apache.curator.framework.CuratorFramework;

import java.util.Map;

@AllArgsConstructor
public class ConfigurationModule extends AbstractModule {

    private final ServiceDiscoveryBundle<AppConfiguration> serviceDiscoveryBundle;
    private final TrouperBundle<AppConfiguration> trouperBundle;


    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public CuratorFramework provideCuratorFramework() {
        return serviceDiscoveryBundle.getCurator();
    }

    @Provides
    @Singleton
    public ServiceEndpointProviderFactory provideServiceEndpointProviderFactory(final CuratorFramework curatorFramework) {
        return new ServiceEndpointProviderFactory(curatorFramework);
    }

    @Provides
    @Singleton
    public RabbitConnection provideRabbitConnection() {
        return trouperBundle.getRabbitConnection();
    }

    @Provides
    @Singleton
    public ActorConfiguration provideActorConfiguration(final AppConfiguration appConfiguration) {
        return appConfiguration.getActorConfiguration();
    }

    @Provides
    @Singleton
    public Map<DockerSubDomain, HttpConfiguration> providesDockerConfigurationMap(final AppConfiguration appConfiguration) {
        return appConfiguration.getDockerConfigurationMap();
    }

    @Provides
    @Singleton
    @DockerErrorCodeMap
    public Map<String, ErrorCode> providesDockerServiceErrorCodeMap() {
        return CommonUtils.getResource(MapperUtils.getMapper(MapperType.JSON),
                "error_code_mappings/docker_service_error_code_mapping.json", this.getClass(), new TypeReference<Map<String, ErrorCode>>() {
                });
    }
}
