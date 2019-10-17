package com.projecty.projectyweb.project.role;

public class NoAdminsInProjectException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String toString() {
        return "Project require to have at least one admin";
    }
}
