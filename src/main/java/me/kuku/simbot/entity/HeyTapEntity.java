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
@Table(name = "hey_tap")
public class HeyTapEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@Column(length = 1000)
	private String cookie;
	@Column(length = 1000)
	private String heyTapCookie;

	public HeyTapEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
	}
}
