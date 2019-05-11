package com.projecty.projectyweb.service.project;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    @Override
    public void save(Project project) {
        projectRepository.save(project);
    }

    @Override
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    public boolean checkIfIsPresentAndContainsCurrentUser(Optional<Project> project) {
        return project.isPresent() && project.get().getUsers().contains(userService.getCurrentUser());
    }


}
