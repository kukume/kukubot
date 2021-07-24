package me.kuku.simbot.service;

import me.kuku.simbot.entity.GroupEntity;

import java.util.List;

public interface GroupService {
	GroupEntity findByGroup(Long group);
	GroupEntity save(GroupEntity groupEntity);
	List<GroupEntity> findAll();
}
