package me.kuku.simbot.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
	private String white;
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
	private Integer maxViolationCount = -1;
	private Integer maxCommandCountOnTime = -1;
	private Boolean locMonitor = false;
	private Boolean flashNotify = false;
	private Boolean repeat = true;
	private Boolean groupAdminAuth;
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
}

