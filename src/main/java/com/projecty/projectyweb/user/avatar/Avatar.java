package com.projecty.projectyweb.user.avatar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projecty.projectyweb.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Entity
@Getter
@Setter
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
}
