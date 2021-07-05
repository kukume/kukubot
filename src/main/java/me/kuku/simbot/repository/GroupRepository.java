package me.kuku.simbot.repository;

import me.kuku.simbot.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<GroupEntity, Integer> {
	GroupEntity findByGroup(Long group);
}
