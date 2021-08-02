package me.kuku.simbot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfficeToken {
	private String accessToken;
	private Long expiresTime;
}
