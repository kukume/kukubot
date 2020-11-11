package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.MotionEntity;

import java.util.List;

@AutoBind
public interface MotionService {
    MotionEntity findByQQ(long qq);
    List<MotionEntity> findAll();
    void save(MotionEntity motionEntity);
    void delByQQ(Long qq);
}