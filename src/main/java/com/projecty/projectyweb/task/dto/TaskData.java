package com.projecty.projectyweb.task.dto;

import com.projecty.projectyweb.task.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskData {
    private Task task;
    private Long projectId;
    private List<String> notAssignedUsernames;
}
