package com.projecty.projectyweb.chat;

public class UsernameLastChatMessageIdDTO {
    private String username;
    private Long lastChatMessageId;

    public UsernameLastChatMessageIdDTO(String username, Long lastChatMessageId) {
        this.username = username;
        this.lastChatMessageId = lastChatMessageId;
    }

    public String getUsername() {
        return username;
    }

    public Long getLastChatMessageId() {
        return lastChatMessageId;
    }
}
