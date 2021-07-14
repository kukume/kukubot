package me.kuku.simbot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IqiYiQrcode {
	private String token;
	private String url;
	private Long expire;
}
