package com.projecty.projectyweb.team.role;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.user.User;

import javax.persistence.*;

@Entity
@Table(name = "teamrole")
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
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id"
    )
    private Team team;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TeamRoles getName() {
        return name;
    }

    public void setName(TeamRoles name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

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
