package me.kuku.simbot.repository;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Integer> {
	MessageEntity findByMessageIdAndGroupEntity(String messageId, GroupEntity groupEntity);
}
