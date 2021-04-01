package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionPojo {
	private String name;
	private String url;
	private String action;
	private List<String> params;
}
