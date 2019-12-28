package com.projecty.projectyweb.chat.socket;

import java.util.Date;

public class OutputSocketChatMessage extends SocketChatMessage {
    private Date sendDate;

    public OutputSocketChatMessage(final String from, final String text, final Date sendDate) {
        setFrom(from);
        setText(text);
        this.sendDate = sendDate;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }
}
