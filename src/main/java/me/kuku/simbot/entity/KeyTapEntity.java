package me.kuku.simbot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@ToString(exclude = "qqEntity")
@Table(name = "key_tap")
public class KeyTapEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@Column(length = 1000)
	private String keyTapCookie;
	private String deviceId;

	public KeyTapEntity(String keyTapCookie, String deviceId){
		this.keyTapCookie = keyTapCookie;
		this.deviceId = deviceId;
	}
}
