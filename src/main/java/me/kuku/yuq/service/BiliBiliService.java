package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.BiliBiliEntity;

import java.util.List;

@AutoBind
public interface BiliBiliService {
    BiliBiliEntity findByQQ(Long qq);
    void save(BiliBiliEntity biliBiliEntity);
    int delByQQ(Long qq);
    List<BiliBiliEntity> findByMonitor(Boolean monitor);
    List<BiliBiliEntity> findAll();
    List<BiliBiliEntity> findByTask(Boolean task);
}
