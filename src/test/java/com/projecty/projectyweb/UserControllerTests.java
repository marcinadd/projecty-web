package com.projecty.projectyweb;

import com.projecty.projectyweb.message.MessageRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
        user.setUsername("user");
        user.setEmail("admin@example.com");
        user.setPassword(encoder.encode("password123"));
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(java.util.Optional.ofNullable(user));
    }

    @Test
    public void givenRequestOnLoginForm_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/user/login"));
    }

    @Test
    public void givenRequestOnRegisterForm_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/user/register"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnSettings_shouldReturnSettingsView() throws Exception {
        mockMvc.perform(get("/settings").flashAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/user/settings"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangePasswordFormWithValidData_shouldRedirectToSettings() throws Exception {
        mockMvc.perform(
                post("/changePassword")
                        .param("currentPassword", "password123")
                        .param("newPassword", "newPassword123")
                        .param("repeatPassword", "newPassword123"))
                .andExpect(view().name("redirect:/settings"));
    }




}
