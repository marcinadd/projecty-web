package com.projecty.projectyweb.chat;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.chat.socket.SocketChatMessage;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class ChatServiceTests {
    @Autowired
    ChatService chatService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    private User user;
    private User admin;
    private User root;
    private ChatMessage lastMessageWithAdmin;
    private ChatMessage lastMessageWithRoot;

    @Before
    public void init() {
        user = new User();
        user.setUsername("user");
        user = userRepository.save(user);

        admin = new User();
        admin.setUsername("admin");
        admin = userRepository.save(admin);

        root = new User();
        root.setUsername("root");
        root = userRepository.save(root);
    }

    public void generateMessages() {
        ChatMessage chatMessage = new ChatMessage(user, root, "ABC!", new Date());
        chatMessage = chatService.save(chatMessage);

        ChatMessage chatMessage1 = new ChatMessage(user, admin, "ABC@", new Date());
        chatService.save(chatMessage1);

        ChatMessage chatMessage2 = new ChatMessage(root, admin, "ABC#", new Date());
        chatService.save(chatMessage2);

        ChatMessage chatMessage3 = new ChatMessage(admin, user, "ABC$", new Date());
        chatMessage3 = chatService.save(chatMessage3);

        lastMessageWithRoot = chatMessage;
        lastMessageWithAdmin = chatMessage3;
    }

    @Test
    @WithMockUser
    public void whenSaveSocketMessage_shouldReturnSavedMessage() {
        String text = "ABC";
        String recipientUsername = "admin";
        SocketChatMessage message = new SocketChatMessage();
        message.setTo(recipientUsername);
        message.setText(text);
        ChatMessage chatMessage = chatService.saveInDatabase(message);
        assertThat(chatMessage.getRecipient().getUsername(), is(recipientUsername));
        assertThat(chatMessage.getText(), is(text));
        assertThat(chatMessage.getId(), is(notNullValue()));
    }

    @Test
    @Transactional
    @WithMockUser
    public void whenGetMessageHistory_shouldReturnMessageHistory() {
        generateMessages();
        List<ChatMessage> messages = chatService.getLastMessagesForDistinctUsers();
        assertThat(messages.contains(lastMessageWithAdmin), is(true));
        assertThat(messages.contains(lastMessageWithRoot), is(true));
        assertThat(messages.size(), is(2));
    }
}
