package com.github.docker.replica.actor.impl;

import com.github.docker.replica.actor.ActorConfiguration;
import com.github.docker.replica.actor.ActorType;
import com.github.docker.replica.actor.BaseActor;
import com.github.docker.replica.actor.message.impl.PostBuildCallbackActorMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.qtrouper.core.models.QAccessInfo;
import io.github.qtrouper.core.models.QueueContext;
import io.github.qtrouper.core.rabbit.RabbitConnection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collections;

@Slf4j
@Singleton
public class PostBuildCallbackActor extends BaseActor {

    @Inject
    public PostBuildCallbackActor(final ActorConfiguration actorConfiguration,
                                  final RabbitConnection connection) {
        super(ActorType.POST_BUILD_CALLBACK, actorConfiguration.getConsumerConfiguration(ActorType.POST_BUILD_CALLBACK), connection, Collections.emptySet());
    }

    @Override
    public boolean handle(QueueContext queueContext, QAccessInfo qAccessInfo) {
        val message = getMessage(queueContext, PostBuildCallbackActorMessage.class);
        log.info("Received Message: {}", message);
        return false;
    }
}
