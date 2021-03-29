package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import com.IceCreamQAQ.Yu.util.IO;
import me.kuku.yuq.pojo.DCloudPojo;
import me.kuku.yuq.pojo.Result;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@AutoBind
public interface DCloudLogic {
	DCloudPojo getData() throws IOException;
	Result<DCloudPojo> login(DCloudPojo dCloudPojo, String email, String password, String code) throws IOException;
	Result<String> upload(DCloudPojo dCloudPojo, String spaceId, String name, InputStream is, Integer size);
	default Result<String> upload(DCloudPojo dCloudPojo, String spaceId, String name, byte[] bytes){
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(bytes);
			return upload(dCloudPojo, spaceId, name, is, null);
		} finally {
			if (is != null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	default Result<String> upload(DCloudPojo dCloudPojo, String spaceId, String name, InputStream is){
		return upload(dCloudPojo, spaceId, name, is, null);
	}
	Result<DCloudPojo> reLogin() throws IOException;
}
