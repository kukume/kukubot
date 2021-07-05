package me.kuku.simbot.repository;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.StepEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepRepository extends JpaRepository<StepEntity, Integer> {
	StepEntity findByQqEntity(QqEntity qqEntity);
}
