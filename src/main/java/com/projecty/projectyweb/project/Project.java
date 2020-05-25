package com.projecty.projectyweb.project;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.task.TaskStatus;
import com.projecty.projectyweb.team.Team;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @Transient
    private List<String> usernames;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id"
    )
    private List<ProjectRole> projectRoles;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id"
    )
    private Team team;

    @Transient
    private Map<TaskStatus, Long> taskSummary;

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tasks=" + tasks +
                ", projectRoles=" + projectRoles +
                '}';
    }
}
