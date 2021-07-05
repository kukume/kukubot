package me.kuku.simbot.service;

import me.kuku.simbot.entity.BiliBiliEntity;
import me.kuku.simbot.entity.QqEntity;

import java.util.List;

public interface BiliBiliService {
	BiliBiliEntity findByQqEntity(QqEntity qqEntity);
	BiliBiliEntity save(BiliBiliEntity entity);
	List<BiliBiliEntity> findAll();
	List<BiliBiliEntity> findByMonitor(Boolean monitor);
	List<BiliBiliEntity> findByTask(Boolean task);
	List<BiliBiliEntity> findByLive(Boolean live);
}
