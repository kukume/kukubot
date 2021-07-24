package me.kuku.simbot.repository;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.MessageEntity;
import me.kuku.simbot.entity.QqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Integer> {
	MessageEntity findByMessageIdAndGroupEntity(String messageId, GroupEntity groupEntity);
	List<MessageEntity> findByGroupEntityAndDateAfter(GroupEntity groupEntity, Date date);
	List<MessageEntity> findByQqEntityAndGroupEntityOrderByDateDesc(QqEntity qqEntity, GroupEntity groupEntity);
}
