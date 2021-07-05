package me.kuku.simbot.repository;

import me.kuku.simbot.entity.HostLocEntity;
import me.kuku.simbot.entity.QqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostLocRepository extends JpaRepository<HostLocEntity, Integer> {
	HostLocEntity findByQqEntity(QqEntity qqEntity);
}
