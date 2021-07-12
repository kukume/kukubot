package me.kuku.simbot.repository;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.WeiboEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeiboRepository extends JpaRepository<WeiboEntity, Integer> {
	WeiboEntity findByQqEntity(QqEntity qqEntity);
	List<WeiboEntity> findByMonitor(Boolean monitor);
}
