package com.projecty.projectyweb.user;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.message.MessageRepository;
import com.projecty.projectyweb.user.avatar.Avatar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.SQLException;

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

    private RegisterForm registerForm;
    private RegisterForm registerFormExistingUser;
    private User user;
    private User userWithAvatar;

    @Before
    public void init() {
        registerForm = new RegisterForm();
        registerForm.setUsername("user2");
        registerForm.setEmail("admin2@example.com");
        registerForm.setPassword("password123");
        registerForm.setPasswordRepeat("password123");
        byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5};
        registerForm.setAvatar(new MockMultipartFile("test", bytes));

        registerFormExistingUser = new RegisterForm();
        registerFormExistingUser.setUsername("user");
        registerFormExistingUser.setEmail("admin@example.com");
        registerFormExistingUser.setPassword("password123");
        registerFormExistingUser.setPasswordRepeat("password123");

        user = new User();
        user = new UserBuilder()
                .username("user")
                .email("admin@example.com")
                .password(encoder.encode("password123"))
                .build();
        user.setId(1L);
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(java.util.Optional.ofNullable(user));

        userWithAvatar = new User();
        userWithAvatar = new UserBuilder()
                .username("userWithAvatar")
                .email("adminWithAvatar@example.com")
                .password(encoder.encode("password567"))
                .build();
        userWithAvatar.setId(12L);
        Avatar avatar = new Avatar();
        avatar.setUser(userWithAvatar);
        avatar.setContentType("image/jpeg");
        try {
            avatar.setFile(new SerialBlob(bytes));
        } catch (SQLException ignored) {
        }
        userWithAvatar.setAvatar(avatar);
        Mockito.when(userRepository.findByUsername(userWithAvatar.getUsername()))
                .thenReturn(java.util.Optional.ofNullable(userWithAvatar));
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

    @Test
    public void givenRequestOnRegisterForExistingUser__shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", registerFormExistingUser.getUsername())
                .param("email",registerFormExistingUser.getEmail())
                .param("password",registerFormExistingUser.getPassword())
                .param("passwordRepeat",registerFormExistingUser.getPassword())
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void givenRequestOnRegister__shouldReturnOk() throws Exception {
        mockMvc.perform(
                post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", registerForm.getUsername())
                        .param("email",registerForm.getEmail())
                        .param("password",registerForm.getPassword())
                        .param("passwordRepeat",registerForm.getPassword())
                )
                .andExpect(status().isOk());

        Mockito.verify(userRepository).findByUsername(registerForm.getUsername());
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        Assert.assertEquals(registerForm.getUsername(), savedUser.getUsername());
        Assert.assertEquals(registerForm.getEmail(), savedUser.getEmail());
        Assert.assertTrue(encoder.matches(registerForm.getPassword(), savedUser.getPassword()));


        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    @WithMockUser
    public void givenRequestForAvatarOnMissingUser_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/user/nouser/avatar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void givenRequestForAvatarWithNoAvatar_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/user/user/avatar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void givenRequestForAvatarWithAvatar_shouldReturnAvatar() throws Exception {
        mockMvc.perform(get("/user/userWithAvatar/avatar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser("user2")
    public void givenRequestForAvatarByAnotherUser_shouldReturnAvatar() throws Exception {
        mockMvc.perform(get("/user/userWithAvatar/avatar"))
                .andExpect(status().isOk());
    }
}

