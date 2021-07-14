package me.kuku.simbot.service;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqVideoEntity;

import java.util.List;

public interface QqVideoService {
	QqVideoEntity findByQqEntity(QqEntity qqEntity);
	List<QqVideoEntity> findAll();
	QqVideoEntity save(QqVideoEntity entity);
}
