package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfficePojo {
	private String name;
	private String clientId;
	private String clientSecret;
	private String tenantId;
	private List<Sku> sku;
	private String domain;


	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Sku{
		private String name;
		private String id;
	}
}
