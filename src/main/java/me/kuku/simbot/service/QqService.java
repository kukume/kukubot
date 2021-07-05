package me.kuku.simbot.service;

import me.kuku.simbot.entity.QqEntity;

import java.util.Optional;

public interface QqService {
	QqEntity findByQq(Long qq);
	QqEntity save(QqEntity entity);
}
