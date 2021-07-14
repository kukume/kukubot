package me.kuku.simbot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "qq_video")
public class QqVideoEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@Column(length = 1000)
	private String cookie;
	@Column(length = 1000)
	private String vuSession;
	@Column(length = 1000)
	private String accessToken;

	public QqVideoEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
	}

	public QqVideoEntity(String cookie){
		this.cookie = cookie;
	}
}
