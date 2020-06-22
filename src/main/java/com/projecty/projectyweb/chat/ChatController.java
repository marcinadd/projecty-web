package com.projecty.projectyweb.chat;

import com.projecty.projectyweb.chat.dto.ChatHistoryData;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("chat")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public Page<ChatMessage> getChatMessages(
            @PathVariable("username") String username,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Optional<User> optionalRecipient = userService.findByByUsername(username);
        if (optionalRecipient.isPresent()) {
            return chatService.findByRecipientAndSenderOrderById(optionalRecipient.get(), offset, limit);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("")
    public List<ChatHistoryData> getChatHistory() {
        return chatService.getChatHistory();
    }

    @GetMapping("/{username}/set/read")
    public void setAllReadForChatWithUser(@PathVariable("username") String username) {
        Optional<User> user = userService.findByByUsername(username);
        if (user.isPresent()) {
            chatService.setAllReadForChat(user.get());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("unreadChatMessageCount")
    public int getUnreadChatMessageCount() {
        return chatService.getUnreadChatMessageCount();
    }
}
