package com.projecty.projectyweb.team.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "teamrole")
@Getter
@Setter
public class TeamRole {

    public TeamRole(TeamRoles name, User user, Team team) {
        this.name = name;
        this.user = user;
        this.team = team;
    }

    public TeamRole() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private TeamRoles name;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private Team team;

    @Override
    public String toString() {
        return "TeamRole{" +
                "id=" + id +
                ", name=" + name +
                ", user=" + user +
                ", team=" + team +
                '}';
    }
}
