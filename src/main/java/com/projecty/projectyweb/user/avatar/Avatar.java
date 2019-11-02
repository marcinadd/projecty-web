package com.projecty.projectyweb.user.avatar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projecty.projectyweb.user.User;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Entity
public class Avatar implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private String contentType;

    @JsonIgnore
    private Blob file;

    @OneToOne
    @MapsId
    @JsonIgnore
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Blob getFile() {
        return file;
    }

    public void setFile(Blob file) {
        this.file = file;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
