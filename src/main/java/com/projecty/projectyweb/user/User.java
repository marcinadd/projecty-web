package com.projecty.projectyweb.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.user.avatar.Avatar;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize(using = UserSerializer.class)
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;

    @CreatedDate
    private LocalDateTime registrationDate;

    private String username;

    // Currently not used
    private String email;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<ProjectRole> projectRoles;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
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
