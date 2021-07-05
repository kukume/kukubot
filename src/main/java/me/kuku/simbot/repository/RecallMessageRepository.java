package me.kuku.simbot.repository;

import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.RecallMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecallMessageRepository extends JpaRepository<RecallMessageEntity, Integer> {
	List<RecallMessageEntity> findByQqEntityAndGroupEntityOrderByDateDesc(QqEntity qqEntity, GroupEntity groupEntity);
}
