package com.github.docker.replica.actor;

import com.github.docker.replica.actor.message.ActorMessage;
import com.github.docker.replica.exceptions.AppException;
import com.github.docker.replica.exceptions.ErrorCode;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.qtrouper.core.models.QueueContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.reflections.Reflections;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class ActorMessagePublisher {
    private static final String BASE_PACKAGE = "com.github.docker.replica";
    private Map<ActorType, BaseActor> actorProvider;

    @Inject
    public ActorMessagePublisher(final Injector injector) {
        Reflections reflections = new Reflections(BASE_PACKAGE);
        final Set<Class<? extends BaseActor>> subClasses = reflections.getSubTypesOf(BaseActor.class);
        actorProvider = new EnumMap<>(ActorType.class);
        actorProvider = subClasses.stream()
                .map(subClass -> BaseActor.class.cast(injector.getInstance(subClass)))
                .collect(Collectors.toMap(BaseActor::getActorType, Function.identity()));
    }

    public void publish(ActorMessage message) {
        try {
            val queueContext = QueueContext.builder().serviceReference(message.getReferenceId()).build();
            queueContext.addContext(ActorMessage.class, message);
            getActor(message.getType()).publish(queueContext);
        } catch (Exception e) {
            log.error("Unable to Publish Message for type: {}, referenceId:{}", message.getType(), message.getReferenceId());
            throw AppException.propagate(e, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private BaseActor getActor(ActorType actorType) {
        if (!actorProvider.containsKey(actorType)) {
            val message = String.format("Can't find a actor with the actorType : %s", actorType);
            log.error(message);
            throw AppException.builder()
                    .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message(message)
                    .build();

        }

        return actorProvider.get(actorType);
    }

}
