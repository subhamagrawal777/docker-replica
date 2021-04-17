package com.github.docker.replica;

import com.github.docker.replica.client.docker.DockerSubDomain;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.phonepe.platform.http.v2.discovery.ServiceEndpointProviderFactory;
import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryBundle;
import lombok.AllArgsConstructor;
import org.apache.curator.framework.CuratorFramework;

import java.util.Map;

@AllArgsConstructor
public class ConfigurationModule extends AbstractModule {

    private final ServiceDiscoveryBundle<AppConfiguration> serviceDiscoveryBundle;


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
    public Map<DockerSubDomain, HttpConfiguration> providesDockerConfigurationMap(final AppConfiguration serviceConfiguration) {
        return serviceConfiguration.getDockerConfigurationMap();
    }
}
