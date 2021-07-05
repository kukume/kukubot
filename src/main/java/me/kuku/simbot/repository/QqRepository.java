package me.kuku.simbot.repository;

import me.kuku.simbot.entity.QqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QqRepository extends JpaRepository<QqEntity, Integer> {
	QqEntity findByQq(Long qq);
}
