package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "oppo_shop")
class OppoShopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 6000)
    var cookie: String = ""
    var earlyToBedClock: Status = Status.OFF
}

interface OppoShopRepository: JpaRepository<OppoShopEntity, Int> {

}

class OppoShopService @Inject constructor(
    private val oppoShopRepository: OppoShopRepository
) {

}