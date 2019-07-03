package com.projecty.projectyweb.project;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
public class ProjectValidatorTests {
    @Autowired
    ProjectValidator projectValidator;

    @Test
    public void givenWhitespaceName_shouldReturnError() {
        Project project = new Project();
        project.setName("       ");
        Errors errors = new BeanPropertyBindingResult(project, "project");
        projectValidator.validate(project, errors);
        assertThat(errors.getFieldErrorCount("name"), greaterThanOrEqualTo(1));
    }

    @Test
    public void givenNullName_shouldReturnError() {
        Project project = new Project();
        Errors errors = new BeanPropertyBindingResult(project, "project");
        projectValidator.validate(project, errors);
        assertThat(errors.getFieldErrorCount("name"), greaterThanOrEqualTo(1));
    }

    @Test
    public void givenCorrectProject_shouldPass() {
        Project project = new Project();
        project.setName("Name");
        Errors errors = new BeanPropertyBindingResult(project, "project");
        projectValidator.validate(project, errors);
        assertThat(errors.hasErrors(), is(false));
    }

    @TestConfiguration
    static class TaskValidatorTestConfiguration {
        @Bean
        public ProjectValidator projectValidator() {
            return new ProjectValidator();
        }
    }
}
