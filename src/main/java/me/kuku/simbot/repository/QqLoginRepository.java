package me.kuku.simbot.repository;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QqLoginRepository extends JpaRepository<QqLoginEntity, Integer> {
	QqLoginEntity findByQqEntity(QqEntity qqEntity);
}
