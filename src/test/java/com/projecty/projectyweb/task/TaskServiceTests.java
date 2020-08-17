package com.projecty.projectyweb.task;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoleService;
import com.projecty.projectyweb.role.Roles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRoleService projectRoleService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectRoleRepository projectRoleRepository;

    @MockBean
    UserService userService;

    private User currentUser;

    @Before
    public void init() {
        currentUser = new User();
        currentUser.setUsername("current-user");
        userRepository.save(currentUser);
        Mockito.when(userService.getCurrentUser())
                .thenReturn(currentUser);
    }

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

    @Test
    @Transactional
    public void whenAssignUserByUsername_shouldReturnTaskWithAssignedUser() {
        User user = new User();
        user.setUsername("user-task");
        user = userRepository.save(user);

        Project project = new Project();
        project.setName("project name");
        project = projectRepository.save(project);

        ProjectRole projectRole = new ProjectRole();
        projectRole.setName(Roles.MANAGER);
        projectRole.setUser(user);
        projectRole.setProject(project);
        projectRoleRepository.save(projectRole);

        List<ProjectRole> projectRoles = new ArrayList<>();
        projectRoles.add(projectRole);
        project.setRoles(projectRoles);
        project = projectRepository.save(project);

        Task task = new Task();
        task.setStartDate(new Date(System.currentTimeMillis()));
        task.setName("task name");
        task.setProject(project);
        task = taskRepository.save(task);

        taskService.assignUserByUsername(task, "user-task");

        Optional<Task> optionalTask = taskRepository.findById(task.getId());
        if (optionalTask.isPresent()) {
            assert optionalTask.get().getAssignedUsers().contains(user);
        } else {
            assert false;
        }
    }

    @Test
    @Transactional
    public void whenGetNotAssignedUsernameListForTask_shouldReturnListStringWithUsernames() {
        User user = new User();
        user.setUsername("user");
        user = userRepository.save(user);

        User notAssigned = new User();
        notAssigned.setUsername("notassigned");
        notAssigned = userRepository.save(notAssigned);

        Project project = new Project();
        project.setName("project name");
        project = projectRepository.save(project);

        ProjectRole projectRole = new ProjectRole();
        projectRole.setName(Roles.MANAGER);
        projectRole.setUser(user);
        projectRole.setProject(project);
        projectRoleRepository.save(projectRole);

        ProjectRole projectRole1 = new ProjectRole();
        projectRole1.setName(Roles.MANAGER);
        projectRole1.setUser(notAssigned);
        projectRole1.setProject(project);
        projectRoleRepository.save(projectRole1);


        Task task = new Task();
        task.setStartDate(new Date(System.currentTimeMillis()));
        task.setName("task name");
        task.setProject(project);
        List<User> assignedUsers = new ArrayList<>();
        assignedUsers.add(user);
        task.setAssignedUsers(assignedUsers);
        task = taskRepository.save(task);

        assertThat(taskService.getNotAssignedUsernameListForTask(task), is(notNullValue()));
    }

    @Test
    @Transactional
    public void whenRemoveAssignmenByUsername_shouldReturnTaskWithoutAssignedUser() {
        Project project = new Project();
        project.setName("Sample name");
        projectRepository.save(project);

        User user = new User();
        user.setUsername("removeAssignment");
        userRepository.save(user);

        Task task = new Task();
        task.setStartDate(new Date(System.currentTimeMillis()));
        task.setName("Sample task");
        task.setProject(project);
        List<User> assignedUsers = new ArrayList<>();
        assignedUsers.add(user);
        task.setAssignedUsers(assignedUsers);
        task = taskRepository.save(task);

        taskService.removeAssignmentByUsername(task, user.getUsername());
        Optional<Task> optionalTask = taskRepository.findById(task.getId());
        if (optionalTask.isPresent()) {
            assertThat(optionalTask.get().getAssignedUsers().size(), is(0));
        } else {
            assert false;
        }
    }

    @Test
    @Transactional
    public void whenCheckIfIsAssigned_shouldReturnTrue() {
        Project project = new Project();
        project.setName("Sample name");
        projectRepository.save(project);

        Task task = new Task();
        task.setStartDate(new Date(System.currentTimeMillis()));
        task.setName("Sample task");
        task.setProject(project);
        List<User> assignedUsers = new ArrayList<>();
        assignedUsers.add(currentUser);
        task.setAssignedUsers(assignedUsers);
        task = taskRepository.save(task);

        assert taskService.hasCurrentUserPermissionToEditOrIsAssignedToTask(task);
    }

    @Test
    @Transactional
    public void whenCheckIfHasPermissionToEdit_shouldReturnTrue() {
        Project project = new Project();
        project.setName("Sample name");
        projectRepository.save(project);

        ProjectRole projectRole = new ProjectRole();
        projectRole.setUser(currentUser);
        projectRole.setProject(project);
        projectRole.setName(Roles.MANAGER);
        projectRoleService.save(projectRole);

        Task task = new Task();
        task.setStartDate(new Date(System.currentTimeMillis()));
        task.setName("Sample task");
        task.setProject(project);
        task = taskRepository.save(task);

        assert taskService.hasCurrentUserPermissionToEditOrIsAssignedToTask(task);
    }

    private Date addToCurrentDate(int days) {
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return new Date(calendar.getTimeInMillis());
    }
}
