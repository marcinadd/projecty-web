package com.projecty.projectyweb.chat.socket;

public class SocketChatMessage {
    private String sender;
    private String recipient;
    private String text;

    public String getSender() {
        return sender;
    }

    void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from='" + sender + '\'' +
                ", to='" + recipient + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
