package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.OfficeToken;
import me.kuku.yuq.pojo.OfficeUserPojo;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;

@AutoBind
public interface OfficeUserLogic {
	Result<?> createUser(OfficeUserPojo officeUserPojo, String displayName, String username, String password, Integer index) throws IOException;
}
