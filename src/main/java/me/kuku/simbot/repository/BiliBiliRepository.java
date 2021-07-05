package me.kuku.simbot.repository;

import me.kuku.simbot.entity.BiliBiliEntity;
import me.kuku.simbot.entity.QqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BiliBiliRepository extends JpaRepository<BiliBiliEntity, Integer> {
	BiliBiliEntity findByQqEntity(QqEntity qqEntity);
	List<BiliBiliEntity> findByMonitor(Boolean monitor);
	List<BiliBiliEntity> findByTask(Boolean task);
	List<BiliBiliEntity> findByLive(Boolean live);
}
