package me.kuku.simbot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "weibo")
@NoArgsConstructor
public class WeiboEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@Column(length = 2000)
	private String pcCookie;
	@Column(length = 2000)
	private String mobileCookie;
	private Boolean monitor = false;

	public WeiboEntity(String pcCookie, String mobileCookie){
		this.pcCookie = pcCookie;
		this.mobileCookie = mobileCookie;
	}

	public WeiboEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
	}
}
