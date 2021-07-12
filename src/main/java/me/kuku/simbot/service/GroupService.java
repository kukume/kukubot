package me.kuku.simbot.service;

import me.kuku.simbot.entity.GroupEntity;

public interface GroupService {
	GroupEntity findByGroup(Long group);
	GroupEntity save(GroupEntity groupEntity);
}
