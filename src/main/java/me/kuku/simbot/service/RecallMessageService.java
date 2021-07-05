package me.kuku.simbot.service;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.RecallMessageEntity;

import java.util.List;

public interface RecallMessageService {
	List<RecallMessageEntity> findByQqEntityAndGroupEntityOrderByDateDesc(QqEntity qqEntity, GroupEntity groupEntity);
	RecallMessageEntity save(RecallMessageEntity entity);
}
