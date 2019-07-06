package com.projecty.projectyweb.task;

import com.projecty.projectyweb.ProjectyWebApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class TaskServiceTests {
    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskService taskService;

    @Test
    public void whenChangeTaskStatus_shouldReturnTaskWithNewStatus() throws ParseException {
        Task task = new Task();
        task.setName("Sample task");
        task.setStartDate(new Date(System.currentTimeMillis()));
        task.setEndDate(new Date(System.currentTimeMillis()));
        task.setStatus(TaskStatus.TO_DO);
        task = taskRepository.save(task);
        taskService.changeTaskStatus(task, "DONE");
        Optional<Task> optionalTask = taskRepository.findById(task.getId());
        if (optionalTask.isPresent()) {
            assertThat(optionalTask.get().getStatus(), is(TaskStatus.DONE));
        } else {
            assert false;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenChangeTaskStatusOnStatusWhichNotExists_shouldThrowIllegalArgumentException() throws ParseException {
        Task task = new Task();
        task.setName("Sample task");
        task.setStartDate(new Date(System.currentTimeMillis()));
        task.setEndDate(new Date(System.currentTimeMillis()));
        task.setStatus(TaskStatus.TO_DO);
        task = taskRepository.save(task);
        taskService.changeTaskStatus(task, "NOT_EXISTS");
        Optional<Task> optionalTask = taskRepository.findById(task.getId());
        if (optionalTask.isPresent()) {
            assertThat(optionalTask.get().getStatus(), is(TaskStatus.DONE));
        } else {
            assert false;
        }
    }


    @Test
    public void whenGetDayCountToStartInFuture_shouldReturnPositiveNumber() {
        Task task = new Task();
        task.setName("Sample task");
        Date startDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DATE, 2);
        startDate = new Date(calendar.getTimeInMillis());
        task.setStartDate(startDate);
        task.setEndDate(Date.valueOf("2100-06-01"));
        task.setStatus(TaskStatus.TO_DO);
        task = taskRepository.save(task);
        assertThat(taskService.getDayCountToStart(task.getId()), greaterThan(0L));
    }

    @Test
    public void whenGetDayCountToStartInPast_shouldReturnNegativeNumber() {
        Task task = new Task();
        task.setName("Sample task");
        Date startDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DATE, -2);
        startDate = new Date(calendar.getTimeInMillis());
        task.setStartDate(startDate);
        task.setEndDate(Date.valueOf("2100-06-01"));
        task.setStatus(TaskStatus.TO_DO);
        task = taskRepository.save(task);
        assertThat(taskService.getDayCountToStart(task.getId()), lessThan(0L));
    }
}
