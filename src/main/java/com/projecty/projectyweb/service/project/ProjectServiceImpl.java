package com.projecty.projectyweb.service.project;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public void save(Project project) {
        projectRepository.save(project);
    }
}
