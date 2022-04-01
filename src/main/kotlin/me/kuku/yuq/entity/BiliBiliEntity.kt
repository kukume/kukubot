package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "bili_bili")
class BiliBiliEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 2000)
    var cookie: String = ""
    var userid: String = ""
    var token: String = ""
    var push: Status = Status.OFF
    var live: Status = Status.OFF
}

interface BiliBiliRepository: JpaRepository<BiliBiliEntity, Int>

class BiliBiliService @Inject constructor(
    private val biliBiliRepository: BiliBiliRepository
) {

}