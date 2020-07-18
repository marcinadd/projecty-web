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
    private Boolean isEmailNotificationEnabled;
    private Boolean isMessageEmailNotificationEnabled;
    //    Project/Teams
    private Boolean canBeAddedToProject;
    private Boolean canBeAddedToTeam;

    @PrePersist
    public void prePersist() {
        if (isEmailNotificationEnabled == null) isEmailNotificationEnabled = true;
        if (isMessageEmailNotificationEnabled == null) isMessageEmailNotificationEnabled = true;
        if (canBeAddedToProject == null) canBeAddedToProject = true;
        if (canBeAddedToTeam == null) canBeAddedToTeam = true;
    }
}
