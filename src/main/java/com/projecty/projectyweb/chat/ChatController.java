package com.projecty.projectyweb.chat;

import com.projecty.projectyweb.chat.dto.ChatHistoryData;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<ChatMessage>> getChatMessages(
            @PathVariable("username") String username,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Optional<User> optionalRecipient = userService.findByByUsername(username);
        if (optionalRecipient.isPresent()) {
            chatService.setAllReadForChat(optionalRecipient.get());
            return new ResponseEntity<>(chatService.findByRecipientAndSenderOrderById(optionalRecipient.get(), offset, limit), HttpStatus.OK);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("")
    public ResponseEntity<List<ChatHistoryData>> getChatHistory() {
        return new ResponseEntity<>(chatService.getChatHistory(), HttpStatus.OK);
    }

    @GetMapping("unreadChatMessageCount")
    public ResponseEntity<Integer> getUnreadChatMessageCount() {
        return new ResponseEntity<>(chatService.getUnreadChatMessageCount(), HttpStatus.OK);
    }
}
