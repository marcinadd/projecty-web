package com.projecty.projectyweb.user;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.message.MessageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserRepository userRepository;

    @MockBean
    MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder encoder;

    private User user;

    @Before
    public void init() {
        user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setEmail("admin@example.com");
        user.setPassword(encoder.encode("password123"));
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(java.util.Optional.ofNullable(user));
    }

    @Test
    @WithMockUser
    public void givenRequestOnSettings_shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.id").value(user.getId()));
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangePasswordFormWithValidData_shouldReturnOk() throws Exception {
        mockMvc.perform(
                post("/changePassword")
                        .param("currentPassword", "password123")
                        .param("newPassword", "newPassword123")
                        .param("repeatPassword", "newPassword123"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnAuth_shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.id").value(user.getId()));
    }
}
