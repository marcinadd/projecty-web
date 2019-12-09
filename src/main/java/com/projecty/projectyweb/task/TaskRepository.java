package com.projecty.projectyweb.task;

import com.projecty.projectyweb.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectAndStatus(Project project, TaskStatus status);
    List<Task> findByProjectAndStatusOrderByStartDate(Project project, TaskStatus status);
    List<Task> findByProjectAndStatusOrderByEndDate(Project project, TaskStatus status);

    Long countByProjectAndStatus(Project project, TaskStatus status);
}
