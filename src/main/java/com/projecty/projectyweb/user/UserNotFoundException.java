package com.projecty.projectyweb.user;

public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public String toString() {
        return "User not found!";
    }
}
