package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DCloudPojo {
	private byte[] captchaImage;
	private String cookie;
	private String token;

	public DCloudPojo(byte[] captchaImage, String cookie){
		this.captchaImage = captchaImage;
		this.cookie = cookie;
	}

	public DCloudPojo(String cookie, String token){
		this.cookie = cookie;
		this.token = token;
	}
}
