package me.kuku.yuq.entity;

import com.icecreamqaq.yudb.jpa.JPADao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "arknights")
public class ArkNightsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique = true)
	private Long qq;
	@Lob
	@Column(columnDefinition = "text")
	private String cookie;

	public ArkNightsEntity(String cookie) {
		this.cookie = cookie;
	}

	public ArkNightsEntity(Long qq) {
		this.qq = qq;
	}
}
