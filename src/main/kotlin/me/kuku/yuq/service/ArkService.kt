package me.kuku.yuq.service

import me.kuku.yuq.entity.UserRecord
import me.kuku.yuq.data.ArkPools
import me.kuku.yuq.data.CardSettle

class ArkService: BaseCardService() {

    override fun UserRecord.getPool() = ArkPools[this.pool]

    override fun UserRecord.invoke(): CardSettle {
        val pool = getPool()!!
        sixFloor++
        fiveFloor++
        fourFloor++
        val r = Math.random()
        return if (r <= (pool.sixProbability + if (sixFloor > 50) (sixFloor - 50) * 0.02 else 0.0)) {
            val c = sixFloor
            sixFloor = 0
            if (pool.upSixFloor == null) CardSettle(6, c, pool, false, isUp = false)
            else {
                val rr = Math.random()
                if (rr > pool.upSixFloor!!) {
                    CardSettle(6, c, pool, false, isUp = false)
                } else CardSettle(7, c, pool, isFloor = false, isUp = false)
            }
        } else if (r <= pool.fiveProbability) {
            val c = fiveFloor
            fiveFloor = 0
            if (pool.upFiveFloor == null) CardSettle(4, c, pool, false, isUp = false)
            else {
                val rr = Math.random()
                if (rr > pool.upFiveFloor!!) {
                    CardSettle(4, c, pool, false, isUp = false)
                } else CardSettle(5, c, pool, isFloor = false, isUp = false)
            }
        } else if (r <= pool.fourProbability) {
            val c = fourFloor
            fourFloor = 0
            if (pool.upFourFloor == null) CardSettle(2, c, pool, false, isUp = false)
            else {
                val rr = Math.random()
                if (rr > pool.upFourFloor!!) {
                    CardSettle(2, c, pool, isFloor = false, isUp = false)
                } else CardSettle(3, c, pool, isFloor = false, isUp = false)
            }
        } else CardSettle(1, 0, pool, false, isUp = false)
    }

}