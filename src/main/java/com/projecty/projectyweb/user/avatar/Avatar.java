package com.projecty.projectyweb.user.avatar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projecty.projectyweb.user.User;

import javax.persistence.*;
import java.sql.Blob;

@Entity
public class Avatar {
    @Id
    private Long userId;

    private String contentType;

    @JsonIgnore
    private Blob file;

    @OneToOne
    @MapsId
    @JsonIgnore
    private User user;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
