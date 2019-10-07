package com.projecty.projectyweb.message.attachment;

import com.projecty.projectyweb.message.Message;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttachmentService {

    public byte[] getByteArrayFromAttachment(Attachment attachment) throws SQLException, IOException {
        Blob blob = attachment.getFile();
        InputStream inputStream = blob.getBinaryStream();
        return IOUtils.toByteArray(inputStream);
    }

    public void addFilesToMessage(MultipartFile[] multipartFiles, Message message) throws IOException, SQLException {
        List<Attachment> attachments = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            Attachment attachment = new Attachment();
            attachment.setFile(new SerialBlob(multipartFile.getBytes()));
            attachment.setFileName(multipartFile.getOriginalFilename());
            attachments.add(attachment);
        }
        message.setAttachments(attachments);
    }
}
