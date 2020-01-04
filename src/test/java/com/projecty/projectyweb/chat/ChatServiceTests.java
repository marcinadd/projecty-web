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
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class ChatServiceTests {
    @Autowired
    ChatService chatService;

    @Autowired
    ChatMessageRepository chatMessageRepository;

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
        if (userRepository.findByUsername("user").isPresent()
                && userRepository.findByUsername("admin").isPresent()
                && userRepository.findByUsername("root").isPresent()) {
            user = userRepository.findByUsername("user").get();
            admin = userRepository.findByUsername("admin").get();
            root = userRepository.findByUsername("root").get();
        } else {
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
        generateMessages();
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

        ChatMessage chatMessage4 = new ChatMessage(root, user, "ABC%", new Date());
        chatService.save(chatMessage4);

        lastMessageWithRoot = chatMessage4;
        lastMessageWithAdmin = chatMessage3;
    }

    @Test
    @WithMockUser
    public void whenSaveSocketMessage_shouldReturnSavedMessage() {
        String text = "ABC";
        String recipientUsername = admin.getUsername();
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
        List<ChatMessageProjection> messages = chatService.getChatHistory();
        Map<ChatMessage, Long> map = messages.stream().collect(Collectors.toMap(ChatMessageProjection::getLastMessage, ChatMessageProjection::getUnreadMessageCount));
        assertThat(map.containsKey(lastMessageWithAdmin), is(true));
        assertThat(map.containsKey(lastMessageWithRoot), is(true));
        assertThat(map.get(lastMessageWithRoot), greaterThanOrEqualTo(1L));
        assertThat(messages.size(), is(2));
    }

    @Test
    @Transactional
    @WithMockUser
    public void whenSetAllRead_shouldAllMessagesWithSpecifiedUserHaveSeenDateSet() {
        chatService.setAllReadForChat(admin);
        List<ChatMessage> list = chatMessageRepository.findBySenderAndCurrentUserWhereSeenDateIsNull(admin, user);
        List<ChatMessage> list2 = chatMessageRepository.findBySenderAndCurrentUserWhereSeenDateIsNull(root, user);
        assertThat(list.size(), is(0));
        assertThat(list2.size(), is(greaterThan(0)));
    }

    @Test
    @Transactional
    @WithMockUser
    public void whenGetMessageCountGroupedById_shouldReturnIdsWithMessageCount() {
        chatService.setAllReadForChat(admin);
        chatService.setAllReadForChat(root);

        // This ChatMessage should not be counted since sender is user
        chatMessageRepository.save(new ChatMessage(user, root, "Unread", new Date()));
        chatMessageRepository.save(new ChatMessage(root, user, "Unread", new Date()));
        chatMessageRepository.save(new ChatMessage(admin, user, "Unread", new Date()));

        Map<Long, Long> map = chatService.getUnreadMessageCountForSpecifiedUserGroupById(user);
        assertThat(map.size(), is(2));
    }

    @Test
    @WithMockUser
    public void whenGetUnreadChatMessageCount_shouldReturnUnreadChatMessageCount() {
        chatMessageRepository.save(new ChatMessage(root, user, "Unread", new Date()));
        chatMessageRepository.save(new ChatMessage(root, user, "Unread", new Date()));

        assertThat(chatService.getUnreadChatMessageCount(), greaterThan(1));
    }
}
