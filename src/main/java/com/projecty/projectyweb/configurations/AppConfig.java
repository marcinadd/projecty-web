package com.projecty.projectyweb.configurations;

import com.projecty.projectyweb.role.Roles;

import java.util.Arrays;
import java.util.List;

public class AppConfig {
    public static final String REDIRECT_MESSAGES_SUCCESS = "messagesSuccess";
    public static final String REDIRECT_MESSAGES_FAILED = "messagesFailed";
    public static final String REDIRECT_MESSAGES = "redirectMessages";
    public static final List<String> ROLE_NAMES = Arrays.asList(Roles.ADMIN.toString(), Roles.USER.toString());
}
