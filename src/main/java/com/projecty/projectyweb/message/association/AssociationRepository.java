package com.projecty.projectyweb.message.association;

import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssociationRepository extends JpaRepository<Association, Long> {
    Optional<Association>  findFirstByUserAndMessage(User user, Message message);
}
