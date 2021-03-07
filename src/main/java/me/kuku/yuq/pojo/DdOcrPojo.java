package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DdOcrPojo {
	private String challenge;
	private String validate;
	private String type;
	private String secCode;
}
