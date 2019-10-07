package com.projecty.projectyweb.message;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.message.attachment.Attachment;
import com.projecty.projectyweb.message.attachment.AttachmentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class AttachmentServiceTests {

    @Autowired
    AttachmentService attachmentService;

    @Test
    public void whenGetByteArrayFromAttachment_shouldReturnByteArray() throws SQLException, IOException {
        Attachment attachment = new Attachment();
        byte[] array = {0, 1, 2, 3, 4, 5, 6};
        attachment.setFile(new SerialBlob(array));
        assertThat(attachmentService.getByteArrayFromAttachment(attachment), is(array));
    }

    @Test
    public void whenAddFilesToMessage_shouldReturnMessageWithFiles() throws IOException, SQLException {
        Message message = new Message();
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = {0, 1, 2, 3, 4, 5, 6};
        MultipartFile multipartFile = new MockMultipartFile(name, originalFileName, contentType, content);
        MultipartFile multipartFile1 = new MockMultipartFile(name, originalFileName, contentType, content);
        attachmentService.addFilesToMessage(new MultipartFile[]{multipartFile, multipartFile1}, message);
        assertThat(message.getAttachments(), hasSize(greaterThan(0)));
        assertThat(message.getAttachments().get(1).getFileName(), is(name));
    }
}
