package me.kuku.simbot.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "net_ease")
public class NetEaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@Column(length = 1000)
	private String musicU;
	@Column(length = 1000)
	private String csrf;

	public NetEaseEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
	}

	public NetEaseEntity(String musicU, String csrf){
		this.musicU = musicU;
		this.csrf = csrf;
	}

	@JSONField(serialize = false)
	public String getCookie(){
		return String.format("os=pc; osver=Microsoft-Windows-10-Professional-build-10586-64bit; appver=2.0.3.131777; channel=netease; __remember_me=true; MUSIC_U=%s; __csrf=%s; ", musicU, csrf);
	}
}
