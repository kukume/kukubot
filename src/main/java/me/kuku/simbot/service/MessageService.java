package me.kuku.simbot.service;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.MessageEntity;

public interface MessageService {
	MessageEntity findByMessageIdAndGroupEntity(String messageId, GroupEntity groupEntity);
	MessageEntity save(MessageEntity entity);
}
