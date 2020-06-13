package com.projecty.projectyweb.project;


import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.task.TaskStatus;
import com.projecty.projectyweb.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
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
    private List<ProjectRole> projectRoles;

    @ManyToOne
    private Team team;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyDate;

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
