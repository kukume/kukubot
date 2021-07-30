package me.kuku.simbot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetEaseQrcode {
	private byte[] bytes;
	private String key;

}
