package com.projecty.projectyweb;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.validator.UserValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class UserValidatorTest {

    @Autowired
    private UserValidator userValidator;
    @MockBean
    private UserRepository userRepository;

    @Before
    public void init() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        Mockito.when(userRepository.findByUsername(admin.getUsername())).thenReturn(admin);
    }

    @Test
    public void userValidationOk() {
        // given
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setPasswordRepeat("password");
        user.setEmail("user@example.com");
        Errors errors = new BeanPropertyBindingResult(user, "user");
        // when
        userValidator.validate(user, errors);
        // then
        assertFalse(errors.hasErrors());
    }

    @Test
    public void userDifferentPasswordRepeat() {
        // given
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setPasswordRepeat("passworda");
        user.setEmail("user@example.com");
        Errors errors = new BeanPropertyBindingResult(user, "user");
        // when
        userValidator.validate(user, errors);
        // then
        assertThat(errors.getFieldErrorCount("passwordRepeat"), greaterThanOrEqualTo(1));
    }

    @Test
    public void userOnlySpacesInFields() {
        //given
        User user = new User();
        user.setUsername("   ");
        user.setPassword("   ");
        user.setPasswordRepeat("   ");
        user.setEmail("   ");
        Errors errors = new BeanPropertyBindingResult(user, "user");
        // when
        userValidator.validate(user, errors);
        System.out.println(errors.getErrorCount());
        assertThat(errors.getFieldErrorCount("username"), greaterThanOrEqualTo(1));
        assertThat(errors.getFieldErrorCount("email"), greaterThanOrEqualTo(1));
        assertThat(errors.getFieldErrorCount("password"), greaterThanOrEqualTo(1));
        assertThat(errors.getFieldErrorCount("passwordRepeat"), greaterThanOrEqualTo(1));
    }

    @Test
    public void userNameExists() {
        //given
        User user = new User();
        user.setUsername("admin");
        user.setPassword("password");
        user.setPasswordRepeat("password");
        user.setEmail("user@example.com");
        Errors errors = new BeanPropertyBindingResult(user, "user");
        // when
        userValidator.validate(user, errors);
        // then
        assertThat(errors.getFieldErrorCount("username"), greaterThanOrEqualTo(1));
    }

    @Test
    public void passwordTooShort() {
        //given
        User user = new User();
        user.setUsername("user");
        user.setPassword("pass");
        user.setPasswordRepeat("pass");
        user.setEmail("user@example.com");
        Errors errors = new BeanPropertyBindingResult(user, "user");
        // when
        userValidator.validate(user, errors);
        // then
        assertThat(errors.getFieldErrorCount("password"), greaterThanOrEqualTo(1));
    }

    @Test
    public void passwordTooLong() {
        //given
        User user = new User();
        user.setUsername("user");
        user.setPassword("passssssssssssssssssssssssssssss");
        user.setPasswordRepeat("passssssssssssssssssssssssssssss");
        user.setEmail("user@example.com");
        Errors errors = new BeanPropertyBindingResult(user, "user");
        // when
        userValidator.validate(user, errors);
        // then
        assertThat(errors.getFieldErrorCount("password"), greaterThanOrEqualTo(1));
    }

    @Test
    public void invalidEmail() {
        //given
        User user = new User();
        user.setUsername("user");
        user.setPassword("pass");
        user.setPasswordRepeat("pass");
        user.setEmail("user@examplecom");

        User user1 = new User();
        user1.setUsername("user");
        user1.setPassword("pass");
        user1.setPasswordRepeat("pass");
        user1.setEmail("userexample.com");

        Errors errors = new BeanPropertyBindingResult(user, "user");
        Errors errors1 = new BeanPropertyBindingResult(user1, "user1");

        // when
        userValidator.validate(user, errors);
        userValidator.validate(user1, errors1);
        // then
        assertThat(errors.getFieldErrorCount("email"), greaterThanOrEqualTo(1));
        assertThat(errors1.getFieldErrorCount("email"), greaterThanOrEqualTo(1));

    }

    @TestConfiguration
    static class UserValidatorTestConfiguration {
        @Bean
        public UserValidator userValidator() {
            return new UserValidator();
        }
    }
}
