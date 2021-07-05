package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.MessageEntity;
import me.kuku.simbot.repository.MessageRepository;
import me.kuku.simbot.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
	@Autowired
	private MessageRepository messageRepository;

	@Override
	public MessageEntity findByMessageIdAndGroupEntity(String messageId, GroupEntity groupEntity) {
		return messageRepository.findByMessageIdAndGroupEntity(messageId, groupEntity);
	}

	@Override
	public MessageEntity save(MessageEntity entity) {
		return messageRepository.save(entity);
	}
}
