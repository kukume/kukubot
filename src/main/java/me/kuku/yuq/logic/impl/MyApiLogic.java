package me.kuku.yuq.logic.impl;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.InstagramPojo;
import me.kuku.yuq.pojo.TwitterPojo;

import java.io.IOException;
import java.util.List;

@AutoBind
public interface MyApiLogic {
    List<TwitterPojo> findTwitterIdByName(String name) throws IOException;
    List<TwitterPojo> findTweetsById(Long id) throws IOException;
    List<InstagramPojo> findInsIdByName(String name) throws IOException;
    List<InstagramPojo> findInsPicById(String name, Long id, Integer page) throws IOException;
}
