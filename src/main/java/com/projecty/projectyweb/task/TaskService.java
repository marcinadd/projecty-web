package com.projecty.projectyweb.task;


import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void changeTaskStatus(Task task, String status) {
        task.setStatus(TaskStatus.valueOf(status));
        taskRepository.save(task);
    }

    public long getDayCountToStart(Long taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Date now = new Date();
            Date start = optionalTask.get().getStartDate();
            return TimeUnit.DAYS.convert(start.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
        }
        return Long.MIN_VALUE;
    }

    public long getDayCountToEnd(Long taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Date now = new Date();
            Date endDate = optionalTask.get().getEndDate();
            return TimeUnit.DAYS.convert(endDate.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
        }
        return Long.MIN_VALUE;
    }

    public void updateTaskDetails(Task existingTask, Task newTask) {
        existingTask.setName(newTask.getName());
        existingTask.setStartDate(newTask.getStartDate());
        existingTask.setEndDate(newTask.getEndDate());
        existingTask.setStatus(newTask.getStatus());
        taskRepository.save(existingTask);
    }
}

