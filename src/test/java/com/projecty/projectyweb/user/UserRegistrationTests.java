package com.projecty.projectyweb.user;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MySQLContainer;

import javax.transaction.Transactional;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static org.junit.Assert.*;

@ActiveProfiles("containers")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRegistrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    int randomServerPort;


    @Autowired
    UserRepository repository;


    @Test
    @Transactional
    public void testDb(){
        RestTemplate rt = new RestTemplate();
        RegisterForm registerForm = new RegisterForm();
        registerForm.setEmail("test@example.com");
        registerForm.setPassword("1234567890");
        registerForm.setPasswordRepeat("1234567890");
        registerForm.setUsername("tester");
        final String baseUrl = "http://localhost:"+randomServerPort+"/register";
        try {
            HttpHeaders headers  = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.set("username", registerForm.getUsername());
            map.set("email",registerForm.getEmail());
            map.set("password",registerForm.getPassword());
            map.set("passwordRepeat",registerForm.getPassword());
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> registerFormResponseEntity = restTemplate.postForEntity(new URI(baseUrl), request, String.class);
            assertEquals(200, registerFormResponseEntity.getStatusCodeValue());
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        }
        assertNotNull(repository.getOne(1L));
        assertEquals("test@example.com",repository.getOne(1L).getEmail());
    }

    @TestConfiguration
    static class Config {

    }
}
