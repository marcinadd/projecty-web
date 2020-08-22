package com.projecty.projectyweb.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projecty.projectyweb.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@MappedSuperclass
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    protected User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    protected User invitedUser;

    protected Roles name;
}
