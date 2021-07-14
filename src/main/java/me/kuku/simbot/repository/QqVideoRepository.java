package me.kuku.simbot.repository;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqVideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QqVideoRepository extends JpaRepository<QqVideoEntity, Integer> {
	QqVideoEntity findByQqEntity(QqEntity qqEntity);
}
