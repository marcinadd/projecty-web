package com.projecty.projectyweb.role;

public enum Roles {
    OWNER("OWNER"),
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
