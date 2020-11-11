package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.GroupEntity;

import java.util.List;

@AutoBind
public interface GroupService {
    void save(GroupEntity groupEntity);
    GroupEntity findByGroup(Long group);
    List<GroupEntity> findByOnTimeAlarm(Boolean onTimeAlarm);
    List<GroupEntity> findAll();
    List<GroupEntity> findByLocMonitor(Boolean locMonitor);
}
