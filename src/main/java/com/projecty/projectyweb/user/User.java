package com.projecty.projectyweb.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.user.avatar.Avatar;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@JsonSerialize(using = UserSerializer.class)
//@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public User(String username,
                String email,
                String password,
                String passwordRepeat,
                List<ProjectRole> projectRoles,
                List<TeamRole> teamRoles,
                List<Message> messagesFrom,
                List<Message> messagesTo,
                List<Task> assignedTasks) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.passwordRepeat = passwordRepeat;
        this.projectRoles = projectRoles;
        this.teamRoles = teamRoles;
        this.messagesFrom = messagesFrom;
        this.messagesTo = messagesTo;
        this.assignedTasks = assignedTasks;
    }

    public User() {
    }

    @Id
    @GeneratedValue
    private Long id;

    @CreatedDate
    private LocalDateTime registrationDate;

    private String username;

    private String email;

    private String password;

    @Transient
    private String passwordRepeat;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProjectRole> projectRoles;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TeamRole> teamRoles;

    @OneToMany(
            mappedBy = "sender",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Message> messagesFrom;

    @OneToMany(
            mappedBy = "recipient",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Message> messagesTo;

    @ManyToMany(
            mappedBy = "assignedUsers",
            cascade = CascadeType.ALL
    )
    private List<Task> assignedTasks;

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.MERGE,
            orphanRemoval = true
    )
    private Avatar avatar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ProjectRole> getProjectRoles() {
        return projectRoles;
    }

    public void setProjectRoles(List<ProjectRole> projectRoles) {
        this.projectRoles = projectRoles;
    }


    public List<TeamRole> getTeamRoles() {
        return teamRoles;
    }

    public void setTeamRoles(List<TeamRole> teamRoles) {
        this.teamRoles = teamRoles;
    }

    public List<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(List<Task> assignedTasks) {
        this.assignedTasks = assignedTasks;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", registrationDate=" + registrationDate +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        User anotherUser = (User) obj;
        if(anotherUser == null) return false;
        if(this.getId() == null && anotherUser.getId() == null) return true;
        if(this.getId() == null || anotherUser.getId() == null) return false;

        return this.getId().equals(anotherUser.id);
    }
}
