package com.projecty.projectyweb.team.role;

import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRoleRepository extends JpaRepository<TeamRole, Long> {

    Optional<TeamRole> findByTeamAndAndUser(Team team, User user);

}
