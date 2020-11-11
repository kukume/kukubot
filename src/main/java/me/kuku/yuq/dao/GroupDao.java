package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.GroupEntity;

import java.util.List;

@Dao
public interface GroupDao extends YuDao<GroupEntity, Integer> {
    GroupEntity findByGroup(Long group);
    List<GroupEntity> findByOnTimeAlarm(Boolean onTimeAlarm);
    @Select("from GroupEntity")
    List<GroupEntity> findAll();
    List<GroupEntity> findByLocMonitor(Boolean locMonitor);
}
