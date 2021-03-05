package me.kuku.yuq.pojo;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("SpellCheckingInspection")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeambitionPojo {
	private String cookie;
	private String strikerAuth;
	private String projectId;
	private String rootId;

	private String panOrgId;
	private String panSpaceId;
	private String panRootId;
	private String panDriveId;
	private String userId;

	public TeambitionPojo(String cookie, String strikerAuth, String projectId, String rootId) {
		this.cookie = cookie;
		this.strikerAuth = strikerAuth;
		this.projectId = projectId;
		this.rootId = rootId;
	}

	public static TeambitionPojo fromConfig(JSONObject jsonObject){
		return new TeambitionPojo(
				jsonObject.getString("cookie"),
				jsonObject.getString("auth"),
				jsonObject.getString("projectId"),
				jsonObject.getString("rootId"),
				jsonObject.getString("panOrgId"),
				jsonObject.getString("panSpaceId"),
				jsonObject.getString("panRootId"),
				jsonObject.getString("panDriveId"),
				jsonObject.getString("userId")
		);
	}
}
