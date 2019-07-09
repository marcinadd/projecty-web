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
        task.setStartDate(addToCurrentDate(2));
        task.setEndDate(Date.valueOf("2100-06-01"));
        task.setStatus(TaskStatus.TO_DO);
        task = taskRepository.save(task);
        assertThat(taskService.getDayCountToStart(task.getId()), greaterThan(0L));
    }

    @Test
    public void whenGetDayCountToStartInPast_shouldReturnNegativeNumber() {
        Task task = new Task();
        task.setName("Sample task");
        task.setStartDate(addToCurrentDate(-2));
        task.setEndDate(Date.valueOf("2100-06-01"));
        task.setStatus(TaskStatus.TO_DO);
        task = taskRepository.save(task);
        assertThat(taskService.getDayCountToStart(task.getId()), lessThan(0L));
    }

    @Test
    public void whenGetDayCountToEndInFuture_shouldReturnPositiveNumber() {
        Task task = new Task();
        task.setName("Sample task");
        task.setStartDate(addToCurrentDate(-2));
        task.setEndDate(addToCurrentDate(2));
        task.setStatus(TaskStatus.TO_DO);
        task = taskRepository.save(task);
        assertThat(taskService.getDayCountToEnd(task.getId()), greaterThan(0L));
    }

    @Test
    public void whenUpdateTaskDetails_shouldUpdateTaskDetails() {
        Task existingTask = new Task();
        existingTask.setName("sample");
        existingTask.setStartDate(new Date(System.currentTimeMillis()));
        existingTask.setStartDate(addToCurrentDate(2));
        existingTask.setStatus(TaskStatus.TO_DO);
        existingTask = taskRepository.save(existingTask);
        Task newTask = new Task();
        newTask.setName("new name");
        newTask.setStartDate(addToCurrentDate(-2));
        newTask.setEndDate(addToCurrentDate(5));
        newTask.setStatus(TaskStatus.DONE);
        taskService.updateTaskDetails(existingTask, newTask);
        Optional<Task> optionalTask = taskRepository.findById(existingTask.getId());
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            assertThat(task.getName(), is(newTask.getName()));
            //TODO Check if date is updated
            assertThat(task.getStatus(), is(newTask.getStatus()));
        } else {
            assert false;
        }
    }

    private Date addToCurrentDate(int days) {
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return new Date(calendar.getTimeInMillis());
    }
}
