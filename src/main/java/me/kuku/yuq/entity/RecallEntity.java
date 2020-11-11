package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "recall", indexes = {@Index(name = "ids_group_qq", columnList = "group_,qq")})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecallEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long qq;
    @Column(name = "group_")
    private Long group;
    @OneToOne
    @JoinColumn(name = "message_id")
    private MessageEntity messageEntity;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
}