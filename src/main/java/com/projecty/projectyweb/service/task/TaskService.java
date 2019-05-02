package com.projecty.projectyweb.service.task;

import com.projecty.projectyweb.model.Task;
import org.springframework.stereotype.Service;

@Service
public interface TaskService {
    void save(Task task);
}
