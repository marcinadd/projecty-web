package com.projecty.projectyweb.project.role;

public enum ProjectRoles {
    OWNER("OWNER"),
    ADMIN("ADMIN"),
    USER("USER");

    private final String role;

    ProjectRoles(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}
