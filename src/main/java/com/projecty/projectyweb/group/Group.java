package com.projecty.projectyweb.group;

import com.projecty.projectyweb.group.role.Role;

import javax.persistence.*;
import java.util.List;


public class Group {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Transient
    private List<String> usernames;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Role> roles;
}
