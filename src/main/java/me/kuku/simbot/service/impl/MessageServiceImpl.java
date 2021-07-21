package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.MessageEntity;
import me.kuku.simbot.repository.MessageRepository;
import me.kuku.simbot.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Override
	public Map<Long, Long> findByGroupEntityAndDateAfter(GroupEntity groupEntity, Date date) {
		List<MessageEntity> list = messageRepository.findByGroupEntityAndDateAfter(groupEntity, date);
		Map<Long, Long> map = new HashMap<>();
		for (MessageEntity messageEntity : list) {
			Long qq = messageEntity.getQqEntity().getQq();
			map.put(qq, map.getOrDefault(qq, 0L) + 1);
		}
		return map;
	}
}
