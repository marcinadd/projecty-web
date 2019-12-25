package com.projecty.projectyweb.chat.socket;

public class OutputSocketChatMessage extends SocketChatMessage {
    private String time;

    OutputSocketChatMessage(final String from, final String text, final String time) {
        setFrom(from);
        setText(text);
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
