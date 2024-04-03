package org.acme.websocket;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(fields = false)
public class ChatMessage {
    private String messageContent;
    private String recipientName;

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
