package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "qqBind", indexes = {@Index(name = "idx_qq", columnList = "qq")})
public class QQBindEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique = true)
	private Long qq;
	@Lob
	@Column(columnDefinition="text")
	private String arkNightsCookie;


	public QQBindEntity(long qq){
		this.qq = qq;
	}
}
