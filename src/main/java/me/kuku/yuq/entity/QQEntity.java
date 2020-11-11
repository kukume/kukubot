package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "QQ")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QQEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long qq;
    @OneToOne
    @JoinColumn(name = "group_id")
    private GroupEntity groupEntity;
    private Integer violationCount;

    public QQEntity(long qq, GroupEntity groupEntity){
        this.qq = qq;
        this.groupEntity = groupEntity;
        this.violationCount = 0;
    }
}