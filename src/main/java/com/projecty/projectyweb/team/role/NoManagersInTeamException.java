package com.projecty.projectyweb.team.role;

public class NoManagersInTeamException extends Exception {
    public String toString() {
        return "Team require to have at least one manager";
    }
}
