package com.projecty.projectyweb.message;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Aspect
@Component
public class MessageAspect {
    private final MessageRepository messageRepository;
    private final MessageService messageService;

    public MessageAspect(MessageRepository messageRepository, MessageService messageService) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
    }

    @Pointcut("execution (* com.projecty.projectyweb.message.MessageController.*(Long,..))" +
            "&&@annotation(com.projecty.projectyweb.configurations.AnyPermission)")
    private void inMessageControllerAndWithAnyPermission() {
    }

    @Before("inMessageControllerAndWithAnyPermission()")
    public void checkIfUserHasPermissionToView(JoinPoint joinPoint) {
        Long messageId = (Long) joinPoint.getArgs()[0];
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (!(optionalMessage.isPresent()
                && messageService.checkIfCurrentUserHasPermissionToView(optionalMessage.get()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
