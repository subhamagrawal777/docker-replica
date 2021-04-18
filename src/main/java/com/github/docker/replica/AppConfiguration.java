package com.github.docker.replica;

import com.github.docker.replica.client.docker.DockerSubDomain;
import com.hystrix.configurator.config.HystrixConfig;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import com.platform.validation.ValidationConfig;
import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.aerospike.config.AerospikeConfiguration;
import io.dropwizard.riemann.RiemannConfig;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.github.qtrouper.core.rabbit.RabbitConfiguration;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class AppConfiguration extends Configuration {

    @NotNull
    @Valid
    private AerospikeConfiguration aerospikeConfiguration;

    @NotNull
    @Valid
    private ServiceDiscoveryConfiguration serviceDiscovery;

    @NotNull
    @Valid
    private SwaggerBundleConfiguration swagger;

    @NotNull
    @Valid
    private HystrixConfig hystrixConfig;

    @NotNull
    @NotEmpty
    private String serviceName;

    @NotNull
    @NotEmpty
    private Map<DockerSubDomain, HttpConfiguration> dockerConfigurationMap;

    @NotNull
    @Valid
    private RiemannConfig riemann = new RiemannConfig();

    private RabbitConfiguration rmqConfig;

    @NotNull
    @Valid
    private ValidationConfig validationConfig;

}
