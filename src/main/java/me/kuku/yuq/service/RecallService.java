package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.RecallEntity;

import java.util.List;

@AutoBind
public interface RecallService {
    List<RecallEntity> findByGroupAndQQ(Long group, Long qq);
    void save(RecallEntity recallEntity);
}
