package com.projecty.projectyweb.message.association;

import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class AssociationService {

    private final UserService userService;

    private final AssociationRepository associationRepository;


    public AssociationService(UserService userService , AssociationRepository associationRepository){
        this.userService = userService;
        this.associationRepository = associationRepository;
    }

    public void recordMessageOfSenderAndRecipient(Message mesage){
        Association senderAssociation = new Association();
        senderAssociation.setMessage(mesage);
        senderAssociation.setUser(mesage.getSender());

        associationRepository.save(senderAssociation);

        Association recipientAssociation = new Association();
        recipientAssociation.setMessage(mesage);
        recipientAssociation.setUser(mesage.getRecipient());

        associationRepository.save(recipientAssociation);
    }

    public void deleteMessageForUser(Message messageToBeDeleted, User user){
        Optional<Association> messageOptional = associationRepository.findFirstByUserAndMessage(user,messageToBeDeleted);
        messageOptional.ifPresent(associationRepository::delete);
    }
    public boolean isVisibleForUser(Message message,User user){
        if(message.getRecipient().equals(user) || message.getSender().equals(user)){
            Optional<Association> messageOptional = associationRepository.findFirstByUserAndMessage(user,message);
            return messageOptional.isPresent();
        }
        return false;
    }
}
