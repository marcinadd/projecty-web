package com.projecty.projectyweb.team.role;

public enum TeamRoles {
    MANAGER("MANAGER"), MEMBER("MEMBER");

    private final String teamRole;

    TeamRoles(String teamRole) {
        this.teamRole = teamRole;
    }

    @Override
    public String toString() {
        return teamRole;
    }
}
