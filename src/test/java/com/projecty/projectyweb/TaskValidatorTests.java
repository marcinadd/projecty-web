package com.projecty.projectyweb;

import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.task.TaskValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
public class TaskValidatorTests {
    @Autowired
    TaskValidator taskValidator;

    @Test
    public void givenCorrectDate_shouldNotReturnAnyErrors() {
        Task task = new Task();
        task.setName("Name");
        task.setStartDate(Date.valueOf("2020-12-31"));
        task.setEndDate(Date.valueOf("2021-12-31"));
        Errors errors = new BeanPropertyBindingResult(task, "task");
        taskValidator.validate(task, errors);
        assertThat(errors.hasErrors(), is(false));
    }

    @Test
    public void givenDateEndAfterStart_shouldReturnErrorOnStartDateField() {
        Task task = new Task();
        task.setName("Name");
        task.setStartDate(Date.valueOf("2020-12-31"));
        task.setEndDate(Date.valueOf("2019-12-31"));
        Errors errors = new BeanPropertyBindingResult(task, "task");
        taskValidator.validate(task, errors);
        assertThat(errors.getFieldErrorCount("startDate"), greaterThanOrEqualTo(1));
    }

    @TestConfiguration
    static class TaskValidatorTestConfiguration {
        @Bean
        public TaskValidator taskValidator() {
            return new TaskValidator();
        }
    }
}
