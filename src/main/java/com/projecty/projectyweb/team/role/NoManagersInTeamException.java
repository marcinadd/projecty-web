package com.projecty.projectyweb.team.role;

public class NoManagersInTeamException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String toString() {
        return "Team require to have at least one manager";
    }
}
