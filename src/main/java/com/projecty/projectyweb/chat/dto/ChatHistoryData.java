package com.projecty.projectyweb.chat.dto;

import com.projecty.projectyweb.chat.ChatMessage;

public class ChatHistoryData {
    private final ChatMessage lastMessage;
    private final long unreadMessageCount;

    public ChatHistoryData(ChatMessage lastMessage, long unreadMessageCount) {
        this.lastMessage = lastMessage;
        this.unreadMessageCount = unreadMessageCount;
    }

    public ChatMessage getLastMessage() {
        return lastMessage;
    }

    public long getUnreadMessageCount() {
        return unreadMessageCount;
    }

    @Override
    public String toString() {
        return "ChatMessageProjection{" +
                "lastMessage=" + lastMessage +
                ", unreadMessageCount=" + unreadMessageCount +
                '}';
    }
}
