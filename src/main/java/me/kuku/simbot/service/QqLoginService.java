package me.kuku.simbot.service;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqLoginEntity;

import java.util.List;

public interface QqLoginService {
	QqLoginEntity findByQqEntity(QqEntity qqEntity);
	List<QqLoginEntity> findAll();
	QqLoginEntity save(QqLoginEntity entity);
	void delete(QqLoginEntity entity);
}
