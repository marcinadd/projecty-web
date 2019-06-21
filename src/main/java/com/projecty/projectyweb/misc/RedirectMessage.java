package com.projecty.projectyweb.misc;

public class RedirectMessage {
    private String text;
    private RedirectMessageTypes type;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public RedirectMessageTypes getType() {
        return type;
    }

    public void setType(RedirectMessageTypes type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "RedirectMessage{" +
                "text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}
