package com.projecty.projectyweb.message.association;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.message.MessageRepository;
import com.projecty.projectyweb.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class AssociationServiceTest {
    @MockBean(name = "messageRepository")
    MessageRepository messageRepository;

    @MockBean(name="associationRepository")
    AssociationRepository associationRepository;



    @Autowired
    private AssociationService service;

    private Association associationForRecipient;
    private Association associationForSender;


    private Message message;


    private User user;
    private User user2;
    @Before
    public void init(){
        user = new User();
        user.setId(2L);
        user.setUsername("user");
        user.setEmail("user@example.com");

        user2 = new User();
        user2.setId(3L);
        user2.setUsername("user2");

        message = new Message();
        message.setId(1L);
        message.setText("This is sample message");
        message.setTitle("sample title");

        message.setRecipient(user2);
        message.setSender(user);


        associationForRecipient = new Association();
        associationForRecipient.setUser(user2);
        associationForRecipient.setMessage(message);
        associationForRecipient.setId(1L);

        associationForSender = new Association();
        associationForSender.setUser(user);
        associationForSender.setMessage(message);
        associationForSender.setId(2L);
    }



    @Test
    public void deleteMessageForUser() {
        Mockito.when(associationRepository.findFirstByUserAndMessage(Mockito.eq(user), Mockito.eq(message)))
                .thenReturn(Optional.of(associationForRecipient));
        service.deleteMessageForUser(message,user);
        verify(associationRepository, times(1)).delete(associationForRecipient);
    }

    @Test
    public void isVisibleForUser() {
        Mockito.when(associationRepository.findFirstByUserAndMessage(Mockito.eq(user), Mockito.eq(message)))
                .thenReturn(Optional.of(associationForRecipient));
        Mockito.when(associationRepository.findFirstByUserAndMessage(Mockito.eq(user2), Mockito.eq(message)))
                .thenReturn(Optional.of(associationForSender));
        assertTrue(service.isVisibleForUser(message,user));
        assertTrue(service.isVisibleForUser(message,user2));
        assertFalse(service.isVisibleForUser(message,new User()));
    }
}