package com.github.docker.replica.actor.message.impl;

import com.github.docker.replica.actor.ActorType;
import com.github.docker.replica.actor.message.ActorMessage;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PostBuildCallbackActorMessage extends ActorMessage {

    private String imageName;
    private String tagName;

    @Builder
    public PostBuildCallbackActorMessage(String imageName, String tagName) {
        super(ActorType.POST_BUILD_CALLBACK);
        this.imageName = imageName;
        this.tagName = tagName;
    }

    public PostBuildCallbackActorMessage() {
        super(ActorType.POST_BUILD_CALLBACK);
    }

    @Override
    public String getReferenceId() {
        return String.format("%s:%s", imageName, tagName);
    }
}
