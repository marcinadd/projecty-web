package com.projecty.projectyweb;

import com.projecty.projectyweb.model.Message;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.MessageRepository;
import com.projecty.projectyweb.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class MessageControllerTests {
    @MockBean
    UserRepository userRepository;

    @MockBean
    MessageRepository messageRepository;

    @Autowired
    private MockMvc mockMvc;

    private Message message;
    private String recipientUsername;

    @Before
    public void init() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setEmail("user@example.com");

        User user1 = new User();
        user1.setId(2L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        message = new Message();
        message.setId(1L);
        message.setText("This is sample message");
        recipientUsername = "user1";
        message.setRecipient(user);
        message.setSender(user1);

        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(user);
        Mockito.when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(user1);
        Mockito.when(messageRepository.findById(message.getId()))
                .thenReturn(Optional.ofNullable(message));
    }

    @Test
    @WithMockUser
    public void givenRequestOnSendMessageToUserWhichNotExists_shouldReturnError() throws Exception {
        mockMvc.perform(post("/messages/sendMessage")
                .flashAttr("message", message)
                .param("recipientUsername", "notExistsUsername"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/sendmessage"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnSendMessageToYourself_shouldReturnError() throws Exception {
        mockMvc.perform(post("/messages/sendMessage")
                .flashAttr("message", message)
                .param("recipientUsername", "user"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/sendmessage"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnSendMessage_shouldRedirectToMessageList() throws Exception {
        mockMvc.perform(post("/messages/sendMessage")
                .flashAttr("message", message)
                .param("recipientUsername", recipientUsername))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/messages/messageList"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnViewMessage_shouldReturnViewMessageView() throws Exception {
        mockMvc.perform(get("/messages/viewMessage?messageId=1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnViewMessageWhichNotExistsOrForbidden_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/messages/viewMessage?messageId=2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void givenRequestOnMessageList_shouldReturnReceivedMessageListView() throws Exception {
        mockMvc.perform(get("/messages/messageList"))
                .andExpect(view().name("fragments/received-message-list"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnMessageListWithTypeReceived_shouldReturnReceivedMessageListView() throws Exception {
        mockMvc.perform(get("/messages/messageList?type=received"))
                .andExpect(view().name("fragments/received-message-list"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnMessageListWithTypeSent_shouldReturnReceivedMessageListView() throws Exception {
        mockMvc.perform(get("/messages/messageList?type=sent"))
                .andExpect(view().name("fragments/received-message-sent"));
    }

}
