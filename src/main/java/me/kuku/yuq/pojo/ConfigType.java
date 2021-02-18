package me.kuku.yuq.pojo;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ConfigType {
	BaiduAIOcrAppId("baiduAIOcrAppId"),
	BaiduAIOcrAppKey("baiduAIOcrAppKey"),
	BaiduAIOcrSecretKey("baiduAIOcrSecretKey"),
	BaiduAIContentCensorAppId("baiduAIContentCensorAppId"),
	BaiduAIContentCensorAppKey("baiduAIContentCensorAppKey"),
	BaiduAIContentCensorSecretKey("baiduAIContentCensorSecretKey"),
	BaiduAISpeechAppId("BaiduAISpeechAppId"),
	BaiduAISpeechAppKey("BaiduAISpeechAppKey"),
	BaiduAISpeechSecretKey("BaiduAISpeechSecretKey"),
	Teambition("teambition"),
	DCloud("dCloud");


	private final String type;
	ConfigType(String type){
		this.type = type;
	}
}
