package com.projecty.projectyweb.project.role;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@JsonSerialize
@Getter
@Setter
public class ProjectRole {
    public ProjectRole(ProjectRoles name, User user, Project project) {
        this.name = name;
        this.user = user;
        this.project = project;
    }

    public ProjectRole() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO Remove this redundancy
    private ProjectRoles name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id"
    )
    private User user;

    @ManyToOne
    @JoinColumn(name = "projectId")
    private Project project;

    @Override
    public String toString() {
        return "ProjectRole{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user;
    }
}
