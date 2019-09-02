package com.projecty.projectyweb.message;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
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
    private byte[] file;

    @Before
    public void init() throws SQLException {
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
        message.setTitle("sample title");
        recipientUsername = "user1";
        message.setRecipient(user);
        message.setSender(user1);
        //message.setFileName("sample.txt");
        file = new byte[]{0, 1, 2, 3, 4, 5};
        //message.setFile(new SerialBlob(file));

        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));
        Mockito.when(messageRepository.findById(message.getId()))
                .thenReturn(Optional.ofNullable(message));
    }

    @Test
    @WithMockUser
    public void givenRequestOnSendMessageToUserWhichNotExists_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/message/sendMessage")
                .flashAttr("message", message)
                .param("recipientUsername", "notExistsUsername"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void givenRequestOnSendMessageToYourself_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/message/sendMessage")
                .flashAttr("message", message)
                .param("recipientUsername", "user"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void givenRequestOnSendMessage_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/message/sendMessage")
                .flashAttr("message", message)
                .param("recipientUsername", recipientUsername))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnViewMessage_shouldReturnMessage() throws Exception {
        mockMvc.perform(get("/message/viewMessage?messageId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(message.getText()));
    }

    @Test
    @WithMockUser
    public void givenRequestOnViewMessageWhichNotExistsOrForbidden_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/message/viewMessage?messageId=2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void givenRequestOnReceivedMessages_shouldReturnReceivedMessages() throws Exception {
        mockMvc.perform(get("/message/receivedMessages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    public void givenRequestOnSentMessages_shouldReturnSentMessages() throws Exception {
        mockMvc.perform(get("/message/sentMessages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    public void givenRequestOnGetUnreadMessageCount_shouldReturnNumber() throws Exception {
        mockMvc.perform(get("/message/getUnreadMessageCount"))
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    @WithMockUser
    public void givenRequestOnDownloadFile_shouldReturnFileToDownload() throws Exception {
        mockMvc.perform(get("/message/downloadFile?messageId=1"))
                .andExpect(status().isOk());
    }
}
