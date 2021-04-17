package com.github.docker.replica;

import com.github.docker.replica.client.docker.DockerSubDomain;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.phonepe.platform.http.v2.common.HttpConfiguration;

import java.util.Map;

public class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public Map<DockerSubDomain, HttpConfiguration> providesDockerConfigurationMap(final AppConfiguration serviceConfiguration) {
        return serviceConfiguration.getDockerConfigurationMap();
    }
}
