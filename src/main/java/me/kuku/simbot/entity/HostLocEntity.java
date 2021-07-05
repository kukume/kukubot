package me.kuku.simbot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "host_loc")
public class HostLocEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	private String username;
	private String password;
	@Column(length = 2000)
	private String cookie;

	public HostLocEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
	}

	public HostLocEntity(String username, String password, String cookie){
		this.username = username;
		this.password = password;
		this.cookie = cookie;
	}
}
