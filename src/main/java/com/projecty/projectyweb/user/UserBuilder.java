package com.projecty.projectyweb.user;

import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.user.avatar.Avatar;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserBuilder {
    private String username;
    private String email;
    private String password;
    private String passwordRepeat;
    private List<ProjectRole> projectRoles;
    private List<TeamRole> teamRoles;
    private List<Message> messagesFrom;
    private List<Message> messagesTo;
    private List<Task> assignedTasks;
    private MultipartFile avatar;

    public UserBuilder username(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder passwordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
        return this;
    }

    public UserBuilder projectRoles(List<ProjectRole> projectRoles) {
        this.projectRoles = projectRoles;
        return this;
    }

    public UserBuilder teamRoles(List<TeamRole> teamRoles) {
        this.teamRoles = teamRoles;
        return this;
    }

    public UserBuilder messagesFrom(List<Message> messagesFrom) {
        this.messagesFrom = messagesFrom;
        return this;
    }

    public UserBuilder messagesTo(List<Message> messagesTo) {
        this.messagesTo = messagesTo;
        return this;
    }

    public UserBuilder assignedTasks(List<Task> assignedTasks) {
        this.assignedTasks = assignedTasks;
        return this;
    }

    public UserBuilder avatar(MultipartFile avatar) {
        this.avatar = avatar;
        return this;
    }

    public User build() {
        final User user = new User(username, email, password, passwordRepeat, projectRoles, teamRoles, messagesFrom, messagesTo, assignedTasks);
        if (avatar != null) {
            Avatar avatar = new Avatar();
            avatar.setContentType(this.avatar.getContentType());
            avatar.setUser(user);
            try {
                avatar.setFile(new SerialBlob(this.avatar.getBytes()));
            } catch (SQLException | IOException ignored) {
            }
            user.setAvatar(avatar);
        }
        return user;
    }
}