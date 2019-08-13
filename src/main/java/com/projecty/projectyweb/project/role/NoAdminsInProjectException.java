package com.projecty.projectyweb.project.role;

public class NoAdminsInProjectException extends RuntimeException {
    public String toString() {
        return "Project require to have at least one admin";
    }
}
