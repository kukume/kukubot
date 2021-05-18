package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.TwitterPojo;

import java.io.IOException;
import java.util.List;

@AutoBind
public interface TwitterLogic {
	List<TwitterPojo> findIdByName(String name) throws IOException;
	List<TwitterPojo> findTweetsById(Long id) throws IOException;
}
