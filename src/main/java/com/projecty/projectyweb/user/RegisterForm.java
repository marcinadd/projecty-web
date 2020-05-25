package com.projecty.projectyweb.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class RegisterForm {
    private String username;
    private String email;
    private String password;
    private String passwordRepeat;
    private MultipartFile avatar;
}
