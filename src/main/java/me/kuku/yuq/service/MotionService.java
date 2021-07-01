package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.StepEntity;

import java.util.List;

@AutoBind
public interface MotionService {
    StepEntity findByQQ(long qq);
    List<StepEntity> findAll();
    void save(StepEntity stepEntity);
    void delByQQ(Long qq);
}