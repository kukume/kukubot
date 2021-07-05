package me.kuku.simbot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bili_bili")
public class BiliBiliEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@Column(length = 2000)
	private String cookie;
	private String userid;
	private String token;
	private Boolean monitor = false;
	@Column(name = "task_")
	private Boolean task = false;
	private Boolean live = false;

	public BiliBiliEntity(String cookie, String userid, String token){
		this.cookie = cookie;
		this.userid = userid;
		this.token = token;
	}

	public BiliBiliEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
	}

}
