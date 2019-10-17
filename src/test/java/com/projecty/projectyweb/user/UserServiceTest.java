package com.projecty.projectyweb.user;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.user.avatar.Avatar;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void whenSaveWithPasswordEncryptWithAvatar_avatarIsSaved() {
        User user = new User();
        user = new UserBuilder()
                .username("userWithAvatar")
                .email("admin@example.com")
                .password("password123")
                .build();
        user.setId(1L);
        Avatar avatar = new Avatar();
        avatar.setContentType("image/jpeg");
        byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5};
        try {
            avatar.setFile(new SerialBlob(bytes));
        } catch (SQLException ignored) {
        }
        avatar.setUser(user);
        user.setAvatar(avatar);

        userService.saveWithPasswordEncrypt(user);

        Optional<User> persistedUser = userRepository.findById(user.getId());

        assertThat(persistedUser, not(Optional.empty()));
        assertThat(persistedUser.get().getAvatar(), not(nullValue()));
        try {
            assertThat(IOUtils.toByteArray(persistedUser.get().getAvatar().getFile().getBinaryStream()), equalTo(IOUtils.toByteArray(avatar.getFile().getBinaryStream())));
        } catch (IOException | SQLException ignore) {
        }
    }
}

