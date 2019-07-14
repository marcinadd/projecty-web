package com.projecty.projectyweb.user;

import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.team.role.TeamRole;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
@Entity
public class User {
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

    public List<Message> getMessagesFrom() {
        return messagesFrom;
    }

    public void setMessagesFrom(List<Message> messagesFrom) {
        this.messagesFrom = messagesFrom;
    }

    public List<Message> getMessagesTo() {
        return messagesTo;
    }

    public void setMessagesTo(List<Message> messagesTo) {
        this.messagesTo = messagesTo;
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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", registrationDate=" + registrationDate +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", passwordRepeat='" + passwordRepeat + '\'' +
                '}';
    }
}
