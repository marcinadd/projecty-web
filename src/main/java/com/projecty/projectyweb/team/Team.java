package com.projecty.projectyweb.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.team.misc.TeamSummary;
import com.projecty.projectyweb.team.role.TeamRole;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Team {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamRole> teamRoles;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Project> projects;

    @Transient
    private List<String> usernames;

    @Transient
    private TeamSummary teamSummary;

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
//                ", teamRoles=" + teamRoles +
                '}';
    }
}
