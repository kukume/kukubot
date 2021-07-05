package me.kuku.simbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "message")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String messageId;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@OneToOne
	@JoinColumn(name = "group_")
	private GroupEntity groupEntity;
	@Column(length = 20000)
	private String content;
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
}
