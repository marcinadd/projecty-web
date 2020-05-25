package com.projecty.projectyweb.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;
import java.util.List;

@Entity
@JsonSerialize
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Date startDate;

    private Date endDate;

    private TaskStatus status;

    @ManyToOne
    @JsonIgnore
    private Project project;

    @ManyToMany
    private List<User> assignedUsers;

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
