package com.projecty.projectyweb.chat;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class ChatControllerTests {
    @MockBean
    UserRepository userRepository;

    @MockBean
    ChatMessageRepository chatMessageRepository;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void init() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        User user = new User();
        user.setUsername("user");
        user.setId(2L);
        Mockito.when(userRepository.findByUsername("user"))
                .thenReturn(java.util.Optional.of(user));
        Mockito.when(userRepository.findByUsername("admin"))
                .thenReturn(java.util.Optional.of(admin));

        List<ChatMessage> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ChatMessage chatMessage = new ChatMessage(admin, user, "text" + i, new Date());
            chatMessage.setId((long) i);
            list.add(chatMessage);
            Mockito.when(chatMessageRepository.findById((long) i))
                    .thenReturn(Optional.of(chatMessage));
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatMessage> page = new PageImpl<>(list, pageable, list.size());
        Mockito.when(chatMessageRepository
                .findByRecipientAndSenderOrderById(any(User.class), any(User.class), any(Pageable.class)))
                .thenReturn(page);

        List<ChatMessage> lastChatMessages = new ArrayList<>();
        ChatMessage lastMessage = new ChatMessage(admin, user, "text", new Date());
        lastMessage.setId(100L);
        lastChatMessages.add(lastMessage);
        Mockito.when(chatMessageRepository.findByIdInIds(anySet()))
                .thenReturn(lastChatMessages);

        List<UserIdChatMessageCountDTO> userIdList = new ArrayList<>();
        userIdList.add(new UserIdChatMessageCountDTO(1L, 50L));
        Mockito.when(chatMessageRepository.countMessagesBySenderWhereSeenDateIsNullGroupBySender(user))
                .thenReturn(userIdList);
    }

    @Test
    @WithMockUser
    public void onGetChatMessages_shouldReturnChatMessages() throws Exception {
        mockMvc.perform(get("/chat/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalPages").value(5));
    }

    @Test
    @WithMockUser
    public void onGetChatMessagesToNotExistingUSer_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/chat/not-existing-username"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void onSetAllRead_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/chat/user/set/read"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void onSetAllReadToNotExistingUsername_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/chat/not-existing-username/set/read"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void onGetChatHistory_shouldReturnChatHistory() throws Exception {
        mockMvc.perform(get("/chat/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unreadMessageCount").value(50))
                .andExpect(jsonPath("$[0].lastMessage.id").value(100L));
    }

    @Test
    @WithMockUser
    public void onGetUnreadChatMessageCount_shouldReturnUnreadChatMessageCount() throws Exception {
        mockMvc.perform(get("/chat/unreadChatMessageCount"))
                .andExpect(status().isOk());
    }

}
