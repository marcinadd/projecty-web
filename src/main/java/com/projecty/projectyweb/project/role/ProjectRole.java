package com.projecty.projectyweb.project.role;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.user.User;

import javax.persistence.*;

@Entity
@JsonSerialize
public class ProjectRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO Remove this redundancy
    private ProjectRoles name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "projectId")
    private Project project;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectRoles getName() {
        return name;
    }

    public void setName(ProjectRoles name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "ProjectRole{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user;
    }
}
