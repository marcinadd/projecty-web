package com.projecty.projectyweb.message.attachment;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.message.association.Association;
import com.projecty.projectyweb.message.association.AssociationRepository;
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

import javax.sql.rowset.serial.SerialBlob;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class AttachmentControllerTests {
    private final UUID uuid = UUID.randomUUID();
    @Autowired
    MockMvc mockMvc;
    @MockBean
    AttachmentRepository attachmentRepository;

    @MockBean
    AssociationRepository associationRepository;

    @MockBean
    UserRepository userRepository;

    @Before
    public void init() throws SQLException {
        User user = User.builder()
                .username("user")
                .build();
        User user1 = User.builder()
                .username("user1")
                .build();
        User user2 = User.builder()
                .username("user2")
                .build();

        Message message = Message.builder()
                .sender(user)
                .recipient(user1)
                .build();
        Attachment attachment = new Attachment();
        attachment.setMessage(message);
        attachment.setId(uuid);
        attachment.setFile(new SerialBlob("Text".getBytes()));
        attachment.setFileName("test.txt");

        Association association = new Association();
        association.setMessage(message);
        association.setUser(user);
        Mockito.when(attachmentRepository.findById(uuid))
                .thenReturn(Optional.of(attachment));
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));
        Mockito.when(userRepository.findByUsername(user2.getUsername()))
                .thenReturn(Optional.of(user2));
        Mockito.when(associationRepository.findFirstByUserAndMessage(user, message))
                .thenReturn(Optional.of(association));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDownloadAttachment_shouldReturnAttachementToDownload() throws Exception {
        mockMvc.perform(get("/attachments/" + uuid))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser("user2")
    public void givenRequestOnDownloadAttachmentWhichNotBelongsToUser_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/attachments/" + uuid))
                .andExpect(status().isOk());
    }

}
