package com.projecty.projectyweb.task;

import com.projecty.projectyweb.project.Project;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProjectTasksData {
    private List<Task> toDoTasks;
    private List<Task> inProgressTasks;
    private List<Task> doneTasks;
    private Project project;
    private Boolean hasPermissionToEdit;
}
