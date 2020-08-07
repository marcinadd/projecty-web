package com.projecty.projectyweb.group.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projecty.projectyweb.group.Group;
import com.projecty.projectyweb.user.User;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

public class Role {
    @Id
    private Long id;

    private Roles name;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne
    @JsonIgnore
    private Group group;
}
