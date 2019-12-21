package com.projecty.projectyweb.socket;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@CrossOrigin("*")
public class SocketController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public SocketController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/secured/room")
    public void sendSpecific(
            @Payload Message msg,
            Principal user,
            @Header("simpSessionId") String sessionId) throws Exception {
        OutputMessage out = new OutputMessage(
                msg.getFrom(),
                msg.getText(),
                new SimpleDateFormat("HH:mm").format(new Date()));
        simpMessagingTemplate.convertAndSendToUser(
                msg.getTo(), "/secured/user/queue/specific-user", out);
    }

}
