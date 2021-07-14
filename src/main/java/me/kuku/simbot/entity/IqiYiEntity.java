package me.kuku.simbot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "i_qi_yi")
public class IqiYiEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@Column(length = 1000)
	private String cookie;
	private String pOne;
	private String pThree;

	public IqiYiEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
	}

	public IqiYiEntity(String cookie, String pOne, String pThree){
		this.cookie = cookie;
		this.pOne = pOne;
		this.pThree = pThree;
	}
}
