package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@AutoBind
public interface QQMailLogic {
    Result<List<Map<String, String>>> getFile(QQLoginEntity qqLoginEntity) throws IOException;
    String fileRenew(QQLoginEntity qqLoginEntity) throws IOException;
}
