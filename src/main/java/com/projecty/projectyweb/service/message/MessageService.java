package com.projecty.projectyweb.service.message;

import com.projecty.projectyweb.model.Message;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface MessageService {
    boolean checkIfCurrentUserHasPermissionToView(Message message);
    void updateSeenDate(Message message);
    void sendMessage(String recipientUsername, Message message, BindingResult bindingResult);
}
