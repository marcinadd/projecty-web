package com.projecty.projectyweb.message;


import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Optional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class MessageValidatorTests {

    @Autowired
    private MessageValidator messageValidator;

    @MockBean
    private UserService userService;

    @Before
    public void init() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setUsername("testUser");

        User user1 = new User();
        user.setId(2L);
        user1.setEmail("test1@test.com");
        user1.setUsername("testUser1");

        User currentUser = new User();
        currentUser.setId(3L);
        currentUser.setEmail("currentUser@test.com");
        currentUser.setUsername("currentUser");
        Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);
        Mockito.when(userService.findByByUsername(user.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(userService.findByByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
    }

    @Test
    @WithMockUser("currentUser")
    public void messageValidationOk() {
        Message message = new Message();
        message.setTitle("Test title");
        message.setText("Test body");
        message.setRecipientUsername("testUser");
        Errors errors = new BeanPropertyBindingResult(message, "message");

        messageValidator.validate(message, errors);
        assertFalse(errors.hasErrors());
    }

    @Test
    @WithMockUser("currentUser")
    public void messageValidationWithEmptyOrNullText() {
        Message message = new Message();
        message.setTitle("Test title");
        message.setRecipientUsername("testUser");
        Errors errors = new BeanPropertyBindingResult(message, "message");
        messageValidator.validate(message, errors);
        assertThat(errors.getFieldErrorCount("text"), greaterThanOrEqualTo(1));

        Message message1 = new Message();
        message.setText("   ");
        message.setTitle("Test title");
        message.setRecipientUsername("testUser");
        errors = new BeanPropertyBindingResult(message1, "message");
        messageValidator.validate(message1, errors);
        assertThat(errors.getFieldErrorCount("text"), greaterThanOrEqualTo(1));
    }

    @Test
    @WithMockUser("currentUser")
    public void messageValidationWithEmptyOrNullTitle() {
        Message message = new Message();
        message.setText("Test text");
        message.setRecipientUsername("testUser");
        Errors errors = new BeanPropertyBindingResult(message, "message");
        messageValidator.validate(message, errors);
        assertThat(errors.getFieldErrorCount("title"), greaterThanOrEqualTo(1));

        Message message1 = new Message();
        message.setText("Test text");
        message.setTitle("    ");
        message.setRecipientUsername("testUser");
        errors = new BeanPropertyBindingResult(message1, "message");
        messageValidator.validate(message1, errors);
        assertThat(errors.getFieldErrorCount("title"), greaterThanOrEqualTo(1));
    }

    @Test
    @WithMockUser("currentUser")
    public void messageValidationForSendOwnSelf() {
        Message message = new Message();
        message.setText("Test text");
        message.setTitle("Test title");
        message.setRecipientUsername("currentUser");
        Errors errors = new BeanPropertyBindingResult(message, "message");
        messageValidator.validate(message, errors);
        assertThat(errors.getFieldErrorCount("recipientUsername"), greaterThanOrEqualTo(1));
    }


    @TestConfiguration
    static class MessageValidatorTestConfiguration {
        @Bean
        public MessageValidator messageValidator() {
            return new MessageValidator();
        }
    }

}
