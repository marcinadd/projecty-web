package com.projecty.projectyweb.message.attachment;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin()
@RestController
@RequestMapping("attachments")
public class AttachmentController {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentRepository attachmentRepository, AttachmentService attachmentService) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentService = attachmentService;
    }


    //    TODO IMPORTANT!!! FIX SECURITY VULNERABILITY HERE
    @GetMapping("{attachmentId}")
//    @AnyPermission
    public @ResponseBody
    byte[] downloadAttachment(
            @PathVariable UUID attachmentId,
            HttpServletResponse response
    ) throws IOException, SQLException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(attachmentId);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + attachment.getFileName());
            response.flushBuffer();
            return attachmentService.getByteArrayFromAttachment(attachment);
        }
        return null;
    }
}
