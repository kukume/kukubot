package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaiduAIPojo {
	private String ocrAppId;
	private String ocrAppKey;
	private String ocrSecretKey;
	private String contentCensorAppId;
	private String contentCensorAppKey;
	private String contentCensorSecretKey;
	private String speechAppId;
	private String speechAppKey;
	private String speechSecretKey;
}
