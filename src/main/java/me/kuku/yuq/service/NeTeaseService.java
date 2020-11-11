package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.NeTeaseEntity;

import java.util.List;

@AutoBind
public interface NeTeaseService {
    NeTeaseEntity findByQQ(Long qq);
    void save(NeTeaseEntity neTeaseEntity);
    List<NeTeaseEntity> findAll();
    int deByQQ(Long qq);
}
