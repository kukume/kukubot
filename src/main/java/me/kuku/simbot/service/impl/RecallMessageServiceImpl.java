package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.RecallMessageEntity;
import me.kuku.simbot.repository.RecallMessageRepository;
import me.kuku.simbot.service.RecallMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecallMessageServiceImpl implements RecallMessageService {

	@Autowired
	private RecallMessageRepository recallMessageRepository;

	@Override
	public List<RecallMessageEntity> findByQqEntityAndGroupEntityOrderByDateDesc(QqEntity qqEntity, GroupEntity groupEntity) {
		return recallMessageRepository.findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity);
	}

	@Override
	public RecallMessageEntity save(RecallMessageEntity entity) {
		return recallMessageRepository.save(entity);
	}
}
