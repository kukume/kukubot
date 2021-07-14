package me.kuku.simbot.repository;

import me.kuku.simbot.entity.IqiYiEntity;
import me.kuku.simbot.entity.QqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IqiYiRepository extends JpaRepository<IqiYiEntity, Integer> {
	IqiYiEntity findByQqEntity(QqEntity qqEntity);
}
