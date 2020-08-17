package com.projecty.projectyweb.project.role;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.role.Role;
import com.projecty.projectyweb.role.Roles;
import com.projecty.projectyweb.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@JsonSerialize
@Getter
@Setter
public class ProjectRole extends Role {

    public ProjectRole(Roles name, User user, Project project) {
        this.name = name;
        this.project = project;
        this.user = user;
    }

    public ProjectRole() {
    }

    public ProjectRole(Roles name, User user, Project project, boolean isInvitation) {
        this.name = name;
        this.project = project;
        if (isInvitation) {
            this.invitedUser = user;
        } else {
            this.user = user;
        }
    }

    @ManyToOne
    @JsonIgnore
    private Project project;

    @Override
    public String toString() {
        return "ProjectRole{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user;
    }
}
