package me.kuku.simbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "recall_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecallMessageEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	@OneToOne
	@JoinColumn(name = "group_")
	private GroupEntity groupEntity;
	@OneToOne
	@JoinColumn(name = "messageId")
	private MessageEntity messageEntity;
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
}
