package com.projecty.projectyweb.chat;

public class ChatMessageProjection {
    private ChatMessage lastMessage;
    private long unreadMessageCount;

    public ChatMessageProjection(ChatMessage lastMessage, long unreadMessageCount) {
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
