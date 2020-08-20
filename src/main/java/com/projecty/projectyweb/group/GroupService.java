package com.projecty.projectyweb.group;

import org.springframework.stereotype.Service;

@Service
public class GroupService<T> {
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Group<T> patchGroup(Group<T> existingGroup, Group<T> patchedGroup) {
        if (!patchedGroup.getName().isEmpty())
            existingGroup.setName(patchedGroup.getName());
        return groupRepository.save(existingGroup);
    }
}
