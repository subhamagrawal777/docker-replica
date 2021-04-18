package com.github.docker.replica.actor;

import com.github.docker.replica.actor.message.ActorMessage;
import io.dropwizard.lifecycle.Managed;
import io.github.qtrouper.Trouper;
import io.github.qtrouper.core.config.QueueConfiguration;
import io.github.qtrouper.core.models.QAccessInfo;
import io.github.qtrouper.core.models.QueueContext;
import io.github.qtrouper.core.rabbit.RabbitConnection;
import lombok.Getter;

import java.util.Set;

public abstract class BaseActor extends Trouper<QueueContext> implements Managed {

    @Getter
    private ActorType actorType;

    public BaseActor(ActorType actorType,
                     QueueConfiguration config,
                     RabbitConnection connection,
                     Set<Class<?>> droppedExceptionTypes) {
        super(actorType.name(), config, connection, QueueContext.class, droppedExceptionTypes);
        this.actorType = actorType;
    }

    public <T> T getMessage(QueueContext queueContext, Class<T> tClass) {
        return tClass.cast(queueContext.getContext(ActorMessage.class));
    }

    @Override
    public boolean process(QueueContext queueContext, QAccessInfo qAccessInfo) {
        return handle(queueContext, qAccessInfo);
    }

    @Override
    public boolean processSideline(QueueContext queueContext, QAccessInfo qAccessInfo) {
        return false;
    }

    public abstract boolean handle(QueueContext queueContext, QAccessInfo qAccessInfo);
}
