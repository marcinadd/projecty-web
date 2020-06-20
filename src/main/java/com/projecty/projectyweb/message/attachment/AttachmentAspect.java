package com.projecty.projectyweb.message.attachment;

import com.projecty.projectyweb.message.MessageService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
public class AttachmentAspect {
    private final AttachmentRepository attachmentRepository;
    private final MessageService messageService;

    public AttachmentAspect(AttachmentRepository attachmentRepository, MessageService messageService) {
        this.attachmentRepository = attachmentRepository;
        this.messageService = messageService;
    }

    @Pointcut("execution (* com.projecty.projectyweb.message.attachment.AttachmentController.*(..))" +
            "&&@annotation(com.projecty.projectyweb.configurations.AnyPermission)")
    private void inAttachmentControllerAndWithAnyPermission() {
    }

    @Before("inAttachmentControllerAndWithAnyPermission()")
    public void checkIfUserHasPermissionToView(JoinPoint joinPoint) {
        UUID attachmentId = (UUID) joinPoint.getArgs()[0];
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(attachmentId);
        if (!(optionalAttachment.isPresent()
                && messageService.checkIfCurrentUserHasPermissionToView(optionalAttachment.get().getMessage()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
