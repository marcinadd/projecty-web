package com.projecty.projectyweb.settings;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settings {
    @Id
    @GeneratedValue
    private Long id;
    //    Notifications
    private Boolean isProjectEmailNotificationsEnabled;
    private Boolean isTeamEmailNotificationsEnabled;
    private Boolean isMessageEmailNotificationsEnabled;
    //    Project/Teams
    private Boolean canBeAddedToProject;
    private Boolean canBeAddedToTeam;

    @PrePersist
    public void prePersist() {
        if (isProjectEmailNotificationsEnabled == null) isProjectEmailNotificationsEnabled = true;
        if (isTeamEmailNotificationsEnabled == null) isTeamEmailNotificationsEnabled = true;
        if (isMessageEmailNotificationsEnabled == null) isMessageEmailNotificationsEnabled = true;
        if (canBeAddedToProject == null) canBeAddedToProject = true;
        if (canBeAddedToTeam == null) canBeAddedToTeam = true;
    }
}
