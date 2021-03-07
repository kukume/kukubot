package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mihoyo")
public class MiHoYoEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique = true)
	private Long qq;
	private String account;
	private String password;
	@Lob
	@Column(columnDefinition = "text")
	private String cookie;
	private String accountId;
	private String ticket;
	private String cookieToken;

	public MiHoYoEntity(Long qq) {
		this.qq = qq;
	}
}
