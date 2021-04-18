package com.github.docker.replica;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.docker.replica.exceptions.AppExceptionMapper;
import com.github.docker.replica.utils.MapperUtils;
import com.github.docker.replica.utils.models.MapperType;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Stage;
import com.hystrix.configurator.core.HystrixConfigurationFactory;
import com.phonepe.fs.AerospikeCacheBundle;
import com.phonepe.platform.requestinfo.RequestInfoBundle;
import com.phonepe.rosey.dwconfig.RoseyConfigSourceProvider;
import com.platform.validation.ValidationBundle;
import com.platform.validation.ValidationConfig;
import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryBundle;
import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryConfiguration;
import io.appform.functionmetrics.FunctionMetricsManager;
import io.dropwizard.Application;
import io.dropwizard.aerospike.config.AerospikeConfiguration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.oor.OorBundle;
import io.dropwizard.riemann.RiemannBundle;
import io.dropwizard.riemann.RiemannConfig;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.github.qtrouper.TrouperBundle;
import io.github.qtrouper.core.rabbit.RabbitConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.zapodot.hystrix.bundle.HystrixBundle;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.util.Collections;
import java.util.List;

@Slf4j
public class App extends Application<AppConfiguration> {

    private List<String> packageNameList = Collections.singletonList(
            "com.github.docker"
    );

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void run(AppConfiguration appConfiguration, Environment environment) {

        FunctionMetricsManager.initialize("commands", environment.metrics());
        environment.jersey().register(AppExceptionMapper.class);
        HystrixConfigurationFactory.init(appConfiguration.getHystrixConfig());

    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        val objectMapper = bootstrap.getObjectMapper();
        setMapperProperties(objectMapper);
        MapperUtils.initialize(ImmutableMap.of(MapperType.JSON, objectMapper));

        val localConfig = Boolean.parseBoolean(System.getenv("USE_LOCAL_CONFIG"));
        log.info("Using localconfig --> {}", localConfig);
        if (localConfig) {
            bootstrap.setConfigurationSourceProvider(
                    new SubstitutingSourceProvider(
                            bootstrap.getConfigurationSourceProvider(),
                            new EnvironmentVariableSubstitutor()));
        } else {
            bootstrap.setConfigurationSourceProvider(
                    new SubstitutingSourceProvider(
                            new RoseyConfigSourceProvider("fse", "mutualfund"),
                            new EnvironmentVariableSubstitutor()));
        }


        val serviceDiscoveryBundle =
                new ServiceDiscoveryBundle<AppConfiguration>() {
                    @Override
                    protected ServiceDiscoveryConfiguration getRangerConfiguration(AppConfiguration config) {
                        return config.getServiceDiscovery();
                    }

                    @Override
                    protected String getServiceName(AppConfiguration config) {
                        return config.getServiceName();
                    }

                    @Override
                    protected int getPort(AppConfiguration config) {
                        return config.getServiceDiscovery().getPublishedPort();
                    }
                };
        bootstrap.addBundle(serviceDiscoveryBundle);

        val aerospikeCacheBundle = aerospikeCacheBundle(objectMapper);
        bootstrap.addBundle(aerospikeCacheBundle);

        val guiceBundle = GuiceBundle.<AppConfiguration>builder()
                .enableAutoConfig(packageNameList.toArray(new String[0]))
                .modules(
                        new ConfigurationModule(serviceDiscoveryBundle)
                )
                .configureFromDropwizardBundles()
                .build(Stage.PRODUCTION);

        bootstrap.addBundle(guiceBundle);

        bootstrap.addBundle(new SwaggerBundle<AppConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(AppConfiguration configuration) {
                return configuration.getSwagger();
            }
        });

        bootstrap.addBundle(HystrixBundle.builder()
                .disableStreamServletInAdminContext()
                .withApplicationStreamPath("/hystrix.stream")
                .build());

        val trouperBundle = new TrouperBundle<AppConfiguration>() {
            @Override
            public RabbitConfiguration getRabbitConfiguration(AppConfiguration configuration) {
                return configuration.getRmqConfig();
            }
        };
        bootstrap.addBundle(trouperBundle);

        bootstrap.addBundle(new RequestInfoBundle());

        bootstrap.addBundle(new RiemannBundle<AppConfiguration>() {
            @Override
            public RiemannConfig getRiemannConfiguration(AppConfiguration configuration) {
                return configuration.getRiemann();
            }
        });

        bootstrap.addBundle(new OorBundle<AppConfiguration>() {
            public boolean withOor() {
                return false;
            }
        });

        bootstrap.addBundle(new ValidationBundle<AppConfiguration>() {
            @Override
            public ValidationConfig getValidationConfig(AppConfiguration configuration) {
                return configuration.getValidationConfig();
            }
        });
    }

    private void setMapperProperties(ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    }

    private AerospikeCacheBundle<AppConfiguration> aerospikeCacheBundle(final ObjectMapper objectMapper) {
        return new AerospikeCacheBundle<AppConfiguration>() {
            @Override
            public AerospikeConfiguration aerospikeConfiguration(AppConfiguration serviceConfiguration) {
                return serviceConfiguration.getAerospikeConfiguration();
            }

            @Override
            public ObjectMapper mapper() {
                return objectMapper;
            }

        };
    }
}
