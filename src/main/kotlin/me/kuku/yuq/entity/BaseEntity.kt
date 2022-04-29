package me.kuku.yuq.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.TypeDef
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.Enumerated
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@TypeDef(name = "json", typeClass = JsonType::class)
open class BaseEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    open var createDate: LocalDateTime = LocalDateTime.now()
    @LastModifiedDate
    open var lastModifiedDate: LocalDateTime = LocalDateTime.now()
    @Enumerated
    open var status: Status = Status.ON
}