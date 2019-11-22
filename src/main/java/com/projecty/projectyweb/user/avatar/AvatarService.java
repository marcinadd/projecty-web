package com.projecty.projectyweb.user.avatar;

import org.springframework.stereotype.Service;

@Service
public class AvatarService {
    private final AvatarRepository avatarRepository;

    public AvatarService(AvatarRepository avatarRepository) {
        this.avatarRepository = avatarRepository;
    }

    public void delete(Avatar avatar) {
        avatarRepository.delete(avatar);
    }
}
