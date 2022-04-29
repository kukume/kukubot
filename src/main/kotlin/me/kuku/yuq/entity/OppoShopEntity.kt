package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "oppo_shop")
class OppoShopEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 6000)
    var cookie: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: OppoShopConfig = OppoShopConfig()

}

interface OppoShopRepository: JpaRepository<OppoShopEntity, Int> {

}

@Service
class OppoShopService (
    private val oppoShopRepository: OppoShopRepository
) {

}

data class OppoShopConfig(var earlyToBedClock: Status = Status.OFF, var sign: Status = Status.OFF)