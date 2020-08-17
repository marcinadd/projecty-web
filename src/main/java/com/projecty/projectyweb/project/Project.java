package com.projecty.projectyweb.project;


import com.projecty.projectyweb.group.Group;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.task.TaskStatus;
import com.projecty.projectyweb.team.Team;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project extends Group<ProjectRole> {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project")
    private List<Task> tasks;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectRole> projectRoleInvitations;

    @ManyToOne
    private Team team;

    @Transient
    private Map<TaskStatus, Long> taskSummary;

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tasks=" + tasks +
                ", projectRoles=" + roles +
                '}';
    }
}
