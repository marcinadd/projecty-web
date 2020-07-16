package com.projecty.projectyweb.settings;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Settings {
    @Id
    @GeneratedValue
    private Long id;
    //    Notifications
    private Boolean isProjectEmailNotificationsEnabled = true;
    private Boolean isTeamEmailNotificationsEnabled = true;
    private Boolean isMessageEmailNotificationsEnabled = true;
    //    Project/Teams
    private Boolean canBeAddedToProject = true;
    private Boolean canBeAddedToTeam = true;
}
