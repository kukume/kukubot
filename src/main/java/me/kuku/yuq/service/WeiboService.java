package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.WeiboEntity;

import java.util.List;

@AutoBind
public interface WeiboService {
    WeiboEntity findByQQ(Long qq);
    void save(WeiboEntity weiboEntity);
    int delByQQ(Long qq);
    List<WeiboEntity> findByMonitor(Boolean monitor);
    List<WeiboEntity> findAll();
}
