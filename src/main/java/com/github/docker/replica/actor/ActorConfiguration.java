package com.github.docker.replica.actor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.qtrouper.core.config.QueueConfiguration;
import io.github.qtrouper.core.config.RetryConfiguration;
import io.github.qtrouper.core.config.SidelineConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorConfiguration {
    private static final String DEFAULT_NAMESPACE = "docker-replica";

    @NotNull
    private Map<ActorType, QueueConfiguration> queueConfigurationMap;

    private static SidelineConfiguration getDefaultConfiguration() {
        return SidelineConfiguration.builder()
                .enabled(true)
                .concurrency(0)
                .build();
    }

    private static RetryConfiguration getDefaultRetryConfiguration() {
        return RetryConfiguration.builder()
                .enabled(true)
                .ttlMs(1000)
                .maxRetries(3)
                .backOffFactor(2)
                .build();
    }

    private static QueueConfiguration getDefaultConfiguration(ActorType actorType) {
        return QueueConfiguration.builder()
                .queueName(actorType.name())
                .concurrency(3)
                .namespace(DEFAULT_NAMESPACE)
                .prefetchCount(1)
                .retry(getDefaultRetryConfiguration())
                .sideline(getDefaultConfiguration())
                .build();
    }

    @JsonIgnore
    public QueueConfiguration getConsumerConfiguration(ActorType actorType) {
        return queueConfigurationMap.getOrDefault(actorType, getDefaultConfiguration(actorType));
    }

}
