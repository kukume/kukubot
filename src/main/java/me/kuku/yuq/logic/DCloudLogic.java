package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.DCloudPojo;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;
import java.io.InputStream;

@AutoBind
public interface DCloudLogic {
	DCloudPojo getData() throws IOException;
	Result<DCloudPojo> login(DCloudPojo dCloudPojo, String email, String password, String code) throws IOException;
	Result<String> upload(DCloudPojo dCloudPojo, String spaceId, String name, InputStream is);
	Result<DCloudPojo> reLogin() throws IOException;
}
