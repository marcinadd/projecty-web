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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
            ChatMessage chatMessage = new ChatMessage(user, admin, "text" + i, new Date());
            list.add(chatMessage);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatMessage> page = new PageImpl<>(list, pageable, list.size());
        Mockito.when(chatMessageRepository
                .findFirstByRecipientOrSenderOrderBySendDate(any(User.class), any(Pageable.class)))
                .thenReturn(page);
    }

    @Test
    public void onGetChatMessages_shouldReturnChatMessages() throws Exception {
        mockMvc.perform(get("/chat/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalPages").value(5));
    }

    @Test
    public void onGetChatMessagesToNotExistingUSer_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/chat/not-existing-username"))
                .andExpect(status().isBadRequest());
    }
}
