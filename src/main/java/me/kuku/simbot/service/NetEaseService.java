package me.kuku.simbot.service;

import me.kuku.simbot.entity.NetEaseEntity;
import me.kuku.simbot.entity.QqEntity;

import java.util.List;

public interface NetEaseService {
	NetEaseEntity findByQqEntity(QqEntity qqEntity);
	List<NetEaseEntity> findAll();
	NetEaseEntity save(NetEaseEntity entity);
}
