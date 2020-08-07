package com.projecty.projectyweb.group.role;

public enum Roles {
    ADMIN("ADMIN"),
    USER("USER");

    private final String role;

    Roles(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}
