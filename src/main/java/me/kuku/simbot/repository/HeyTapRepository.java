package me.kuku.simbot.repository;

import me.kuku.simbot.entity.HeyTapEntity;
import me.kuku.simbot.entity.QqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeyTapRepository extends JpaRepository<HeyTapEntity, Integer> {
	HeyTapEntity findByQqEntity(QqEntity qqEntity);
}
