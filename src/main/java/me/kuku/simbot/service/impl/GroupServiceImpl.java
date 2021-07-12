package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.repository.GroupRepository;
import me.kuku.simbot.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

	@Autowired
	private GroupRepository groupRepository;

	@Override
	public GroupEntity findByGroup(Long group) {
		return groupRepository.findByGroup(group);
	}

	@Override
	public GroupEntity save(GroupEntity groupEntity) {
		return groupRepository.save(groupEntity);
	}
}
