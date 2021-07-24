package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.repository.GroupRepository;
import me.kuku.simbot.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

	@Override
	public List<GroupEntity> findAll() {
		return groupRepository.findAll();
	}

	@Override
	public void deleteByGroup(Long group) {
		groupRepository.deleteByGroup(group);
	}

	@Override
	public void delete(GroupEntity groupEntity) {
		groupRepository.delete(groupEntity);
	}
}
