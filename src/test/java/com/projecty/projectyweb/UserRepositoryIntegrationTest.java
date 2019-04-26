package com.projecty.projectyweb;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryIntegrationTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    public void whenFindByUserName_thenReturnUser(){
        String username="admin";
        User user=new User();
        user.setUsername(username);
        userRepository.save(user);


        User found = userRepository.findByUsername(username);
        assertThat(found.getUsername().equals(user.getUsername()));

    }





}
