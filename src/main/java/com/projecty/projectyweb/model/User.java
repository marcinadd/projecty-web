package com.projecty.projectyweb.model;

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
    private List<Role> roles;
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
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
