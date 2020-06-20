package com.projecty.projectyweb.message.attachment;

import com.projecty.projectyweb.ProjectyWebApplication;
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

    @Before
    public void init() throws SQLException {
        Attachment attachment = new Attachment();
        attachment.setId(uuid);
        attachment.setFile(new SerialBlob("Text".getBytes()));
        attachment.setFileName("test.txt");
        Mockito.when(attachmentRepository.findById(uuid))
                .thenReturn(Optional.of(attachment));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDownloadAttachment_shouldReturnAttachementToDownload() throws Exception {
        mockMvc.perform(get("/attachments/" + uuid))
                .andExpect(status().isOk());
    }
}
