package me.kuku.simbot.repository;

import me.kuku.simbot.entity.NetEaseEntity;
import me.kuku.simbot.entity.QqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetEaseRepository extends JpaRepository<NetEaseEntity, Integer> {
	NetEaseEntity findByQqEntity(QqEntity qqEntity);
}
