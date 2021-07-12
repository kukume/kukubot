package me.kuku.simbot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_")
@NoArgsConstructor
@Getter
@Setter
public class GroupEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique = true, name = "group_")
	private Long group;
	private Boolean status = false;
	@ManyToMany(mappedBy = "groups")
	private Set<QqEntity> qqEntities = new HashSet<>();

	public GroupEntity(Long group){
		this.group = group;
	}

	public QqEntity getQq(Long qq){
		for (QqEntity qqEntity : qqEntities) {
			if (qqEntity.getQq().equals(qq)) return qqEntity;
		}
		return null;
	}
}

