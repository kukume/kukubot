package me.kuku.simbot.service;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.MessageEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MessageService {
	MessageEntity findByMessageIdAndGroupEntity(String messageId, GroupEntity groupEntity);
	MessageEntity save(MessageEntity entity);
	Map<Long, Long> findByGroupEntityAndDateAfter(GroupEntity groupEntity, Date date);
}
