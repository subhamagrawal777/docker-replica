package com.github.docker.replica.actor.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.docker.replica.actor.ActorType;
import com.github.docker.replica.actor.message.impl.PostBuildCallbackActorMessage;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = ActorType.POST_BUILD_CALLBACK_TEXT, value = PostBuildCallbackActorMessage.class)
})
public abstract class ActorMessage {
    private final ActorType type;

    public abstract String getReferenceId();
}
