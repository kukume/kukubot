package me.kuku.simbot.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_")
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "qqEntities")
public class GroupEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique = true, name = "group_")
	private Long group;
	private Boolean status = false;
	@ManyToMany(mappedBy = "groups")
	private Set<QqEntity> qqEntities = new HashSet<>();
	@Lob
	@Column(columnDefinition = "text")
	private String black;
	@Lob
	@Column(columnDefinition = "text")
	private String violation;
	@Lob
	@Column(columnDefinition = "text")
	private String qa;
	@Lob
	@Column(columnDefinition = "text")
	private String admin;
	@Lob
	@Column(columnDefinition = "text")
	private String weibo;
	@Lob
	@Column(columnDefinition = "text")
	private String biliBili;
	@Lob
	@Column(columnDefinition = "text")
	private String biliBiliLive;
	@Lob
	@Column(columnDefinition = "text")
	private String intercept;
	@Lob
	@Column(columnDefinition = "text")
	private String commandLimit;
	@Lob
	@Column(columnDefinition = "text")
	private String shellCommand;
	private Boolean colorPic = false;
	private Boolean recall = false;
	private Boolean pic = false;
	private Boolean leaveGroupBlack = false;
	private Boolean autoReview = false;
	private Boolean onTimeAlarm = false;
	private Integer maxCommandCountOnTime = -1;
	private Boolean locMonitor = false;
	private Boolean flashNotify = false;
	private Boolean repeat = true;
	private Boolean groupAdminAuth = false;
	private Boolean kickWithoutSpeaking = false;

	public GroupEntity(Long group){
		this.group = group;
	}

	public QqEntity getQq(Long qq){
		for (QqEntity qqEntity : qqEntities) {
			if (qqEntity.getQq().equals(qq)) return qqEntity;
		}
		return null;
	}

	public JSONArray getBlackJson(){
		return black == null ? new JSONArray() : JSON.parseArray(black);
	}

	public void setBlackJson(JSONArray json){
		this.black = json.toJSONString();
	}

	public JSONArray getViolationJson(){
		return violation == null ? new JSONArray() : JSON.parseArray(violation);
	}

	public void setViolationJson(JSONArray json){
		this.violation = json.toJSONString();
	}

	public JSONArray getQaJson(){
		return qa == null ? new JSONArray() : JSON.parseArray(qa);
	}

	public void setQaJson(JSONArray json){
		this.qa = json.toJSONString();
	}

	public JSONArray getAdminJson(){
		return admin == null ? new JSONArray() : JSON.parseArray(admin);
	}

	public void setAdminJson(JSONArray json){
		this.admin = json.toJSONString();
	}

	public JSONArray getWeiboJson(){
		return weibo == null ? new JSONArray() : JSON.parseArray(weibo);
	}

	public void setWeiboJson(JSONArray json){
		this.weibo = json.toJSONString();
	}

	public JSONArray getBiliBiliJson(){
		return biliBili == null ? new JSONArray() : JSON.parseArray(biliBili);
	}

	public void setBiliBiliJson(JSONArray json){
		this.biliBili = json.toJSONString();
	}

	public JSONArray getBiliBiliLiveJson(){
		return biliBiliLive == null ? new JSONArray() : JSON.parseArray(biliBiliLive);
	}

	public void setBiliBiliLiveJson(JSONArray json){
		this.biliBiliLive = json.toJSONString();
	}

	public JSONArray getInterceptJson(){
		return intercept == null ? new JSONArray() : JSON.parseArray(intercept);
	}

	public void setInterceptJson(JSONArray json){
		this.intercept = json.toJSONString();
	}

	public JSONObject getCommandLimitJson(){
		return commandLimit == null ? new JSONObject() : JSON.parseObject(commandLimit);
	}

	public void setCommandLimitJson(JSONObject json){
		this.commandLimit = json.toJSONString();
	}

	public JSONArray getShellCommandJson(){
		return shellCommand == null ? new JSONArray() : JSON.parseArray(shellCommand);
	}

	public void setShellCommandJson(JSONArray json){
		this.shellCommand = json.toJSONString();
	}
}

