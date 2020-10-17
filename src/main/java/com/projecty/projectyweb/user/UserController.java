package com.projecty.projectyweb.user;

import com.projecty.projectyweb.configurations.AnyPermission;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@CrossOrigin()
@RestController
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("auth")
    public ResponseEntity<User> getUser() {
        return new ResponseEntity<>(userService.getCurrentUser(), HttpStatus.OK);
    }

    @GetMapping("user/{username}/avatar")
    @AnyPermission
    public @ResponseBody
    byte[] getAvatar(
            @PathVariable String username,
            HttpServletResponse response
    ) throws IOException, SQLException {
        Optional<User> maybeUser = userRepository.findByUsername(username);
        User user = maybeUser.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getAvatar() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        response.setContentType(user.getAvatar().getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=avatar-" + username);
        response.flushBuffer();
        return IOUtils.toByteArray(user.getAvatar().getFile().getBinaryStream());
    }

    @PostMapping("user/avatar")
    public void setAvatar(@RequestParam("avatar") MultipartFile multipartFile) throws IOException, SQLException {
        userService.setUserAvatar(multipartFile);
    }

    @GetMapping("users/usernames")
    public ResponseEntity<List<String>> getUsernamesStartWith(@RequestParam String usernameStartsWith) {
        return new ResponseEntity<>(userService.getUsernamesStartWith(usernameStartsWith), HttpStatus.OK);
    }
}
