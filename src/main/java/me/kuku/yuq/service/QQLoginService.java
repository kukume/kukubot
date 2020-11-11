package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.QQLoginEntity;

import java.util.List;

@AutoBind
public interface QQLoginService {
    QQLoginEntity findByQQ(Long qq);
    void save(QQLoginEntity qqLoginEntity);
    void delByQQ(Long qq);
    List<QQLoginEntity> findByActivity();
}
