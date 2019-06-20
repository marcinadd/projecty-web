package com.projecty.projectyweb.helpers;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.model.Project;

import java.util.List;

public interface UserHelper {
    List<String> cleanUsernames(List<String> usernames);

    List<String> removeExistingUsernamesInProject(List<String> usernames, Project project, List<RedirectMessage> messages);
}
