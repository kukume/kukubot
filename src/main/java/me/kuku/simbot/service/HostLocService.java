package me.kuku.simbot.service;

import me.kuku.simbot.entity.HostLocEntity;
import me.kuku.simbot.entity.QqEntity;

import java.util.List;

public interface HostLocService {
	HostLocEntity findByQqEntity(QqEntity qqEntity);
	HostLocEntity save(HostLocEntity entity);
	List<HostLocEntity> findAll();
	List<HostLocEntity> findByMonitor(Boolean monitor);
	List<HostLocEntity> findBySign(Boolean sign);
}
