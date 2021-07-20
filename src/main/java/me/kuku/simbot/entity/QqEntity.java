package me.kuku.simbot.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "qq")
@NoArgsConstructor
@Getter
@Setter
public class QqEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique = true)
	private Long qq;
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "qq_group", joinColumns = {@JoinColumn(name = "qq_id")},
			inverseJoinColumns = {@JoinColumn(name = "group_id")})
	private Set<GroupEntity> groups = new HashSet<>();

	public Long getQq(){
		return qq;
	}

	public QqEntity(Long qq){
		this.qq = qq;
	}

	public GroupEntity getGroup(Long group){
		for (GroupEntity groupEntity : groups) {
			if (group.equals(groupEntity.getGroup())) return groupEntity;
		}
		return null;
	}

}
