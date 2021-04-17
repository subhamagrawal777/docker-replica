package com.github.docker.replica.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.phonepe.platform.http.v2.common.ServiceEndpointProvider;
import com.phonepe.platform.http.v2.common.StaticServiceEndpointProvider;
import com.phonepe.platform.http.v2.discovery.RangerEndpointProvider;
import io.appform.dropwizard.discovery.client.ServiceDiscoveryClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

@Slf4j
@Getter
public abstract class BaseServiceProvider {

    private HttpConfiguration httpConfiguration;
    private ServiceEndpointProvider serviceEndpointProvider;
    private ServiceDiscoveryClient client;

    public BaseServiceProvider(HttpConfiguration httpConfiguration,
                               ObjectMapper mapper,
                               CuratorFramework curatorFramework) {
        this.httpConfiguration = httpConfiguration;

        if (httpConfiguration.isUsingZookeeper()) {
            this.client = ServiceDiscoveryClient.fromCurator()
                    .curator(curatorFramework)
                    .objectMapper(mapper)
                    .namespace("phonepe")
                    .serviceName(httpConfiguration.getServiceName())
                    .environment(httpConfiguration.getEnvironment())
                    .build();
        }

        this.serviceEndpointProvider = httpConfiguration.isUsingZookeeper()
                ? new RangerEndpointProvider(this.getClient())
                : new StaticServiceEndpointProvider(httpConfiguration);
    }

    public void start() throws Exception {
        if (null != client) {
            this.client.start();
        }
    }

    public void stop() throws Exception {
        if (null != client) {
            this.client.stop();
        }
    }
}
