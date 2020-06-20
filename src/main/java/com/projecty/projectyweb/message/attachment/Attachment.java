package com.projecty.projectyweb.message.attachment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projecty.projectyweb.message.Message;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.sql.Blob;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Attachment {
    @ManyToOne
    @JsonIgnore
    private Message message;

    public Attachment() {

    }

    @Id
    @GeneratedValue
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;

    private String fileName;

    @JsonIgnore
    private Blob file;

    public Attachment(String fileName, Blob file, Message message) {
        this.fileName = fileName;
        this.file = file;
        this.message = message;
    }
}
