package com.projecty.projectyweb.chat;

public class UserIdChatMessageCountDTO {
    private Long userId;
    private Long unreadMessageCount;

    public UserIdChatMessageCountDTO(Long userId, Long unreadMessageCount) {
        this.userId = userId;
        this.unreadMessageCount = unreadMessageCount;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getUnreadMessageCount() {
        return unreadMessageCount;
    }

    @Override
    public String toString() {
        return "UserIdChatMessageCountDTO{" +
                "userId=" + userId +
                ", messageCount=" + unreadMessageCount +
                '}';
    }
}
