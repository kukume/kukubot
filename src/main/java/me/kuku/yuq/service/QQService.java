package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.QQEntity;

import java.util.List;

@AutoBind
public interface QQService {
    QQEntity findByQQAndGroup(Long qq, Long group);
    void save(QQEntity qqEntity);
    @SuppressWarnings("UnusedReturnValue")
    int delByQQAndGroup(Long qq, Long group);
    List<QQEntity> findAll();
    List<QQEntity> findByHostLocPush(boolean hostLocPush);
}