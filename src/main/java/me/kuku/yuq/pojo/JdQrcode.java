package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.kuku.pojo.QqLoginQrcode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JdQrcode {
	private QqLoginQrcode qqLoginQrcode;
	private String tempCookie;
	private String state;
	private String redirectUrl;
	private String lSid;
}
