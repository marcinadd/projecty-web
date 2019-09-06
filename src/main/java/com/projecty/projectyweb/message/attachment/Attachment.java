package com.projecty.projectyweb.message.attachment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Blob;

@Entity
public class Attachment {
    public Attachment(String fileName, Blob file) {
        this.fileName = fileName;
        this.file = file;
    }

    public Attachment() {

    }
    @Id
    @GeneratedValue
    private long id;

    private String fileName;

    @JsonIgnore
    private Blob file;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Blob getFile() {
        return file;
    }

    public void setFile(Blob file) {
        this.file = file;
    }
}
