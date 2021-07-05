package me.kuku.simbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "step")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StepEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;

	private String leXinPhone;
	private String leXinPassword;
	@Column(length = 5000)
	private String leXinCookie;
	private String leXinUserid;
	@Column(length = 5000)
	private String leXinAccessToken;

	private String miPhone;
	private String miPassword;
	@Column(length = 1000)
	private String miLoginToken;

	private Integer step;

	public StepEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
		this.step = -1;
	}

	public StepEntity(String leXinPhone, String leXinPassword, String leXinCookie, String leXinUserid, String leXinAccessToken){
		this.leXinPhone = leXinPhone;
		this.leXinPassword = leXinPassword;
		this.leXinCookie = leXinCookie;
		this.leXinUserid = leXinUserid;
		this.leXinAccessToken = leXinAccessToken;
		step = -1;
	}

	public StepEntity(String miPhone, String miPassword, String miLoginToken){
		this.miPhone = miPhone;
		this.miPassword = miPassword;
		this.miLoginToken = miLoginToken;
		this.step = -1;
	}
}
