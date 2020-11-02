package me.kuku.yuq.service

import me.kuku.yuq.data.YuanShenPools
import me.kuku.yuq.data.CardSettle
import me.kuku.yuq.entity.UserRecord
import me.kuku.yuq.service.BaseCardService

class YuanShenService: BaseCardService() {

    override fun UserRecord.getPool() = YuanShenPools[this.pool]

    override fun UserRecord.invoke(): CardSettle {
        val pool = getPool()!!
        fiveFloor++
        fourFloor++
        if (fiveFloor >= pool.fiveFloor) {
            fiveFloor = 0
            return if (pool.upFloor != null) {
                if (upFive) {
                    upFive = false
                    CardSettle(5, fiveFloor, pool, isFloor = true, isUp = true)
                } else {
                    val r = Math.random()
                    if (r > pool.upFloor!!) {
                        upFive = true
                        CardSettle(4, fiveFloor, pool, isFloor = true, isUp = false)
                    } else CardSettle(5, fiveFloor, pool, isFloor = true, isUp = false)
                }
            } else CardSettle(4, fiveFloor, pool, true, isUp = false)
        }
        if (fourFloor >= pool.fourFloor) {
            fourFloor = 0
            return if (pool.upFloor != null) {
                if (upFour) {
                    upFour = false
                    CardSettle(3, fourFloor, pool, isFloor = true, isUp = true)
                } else {
                    val r = Math.random()
                    if (r > pool.upFloor!!) {
                        upFour = true
                        CardSettle(2, fourFloor, pool, isFloor = true, isUp = false)
                    } else CardSettle(3, fourFloor, pool, isFloor = true, isUp = false)
                }
            } else CardSettle(2, fourFloor, pool, isFloor = true, isUp = false)
        }
        val r = Math.random()
        return if (r <= pool.fiveProbability) {
            val c = fiveFloor
            fiveFloor = 0
            if (pool.upFloor == null) CardSettle(4, c, pool, false, isUp = false)
            else if (upFive) {
                upFive = false
                CardSettle(5, c, pool, isFloor = false, isUp = true)
            } else {
                val rr = Math.random()
                if (rr > pool.upFloor!!) {
                    upFive = true
                    CardSettle(4, c, pool, false, isUp = false)
                } else CardSettle(5, c, pool, isFloor = false, isUp = false)
            }
        } else if (r <= pool.fourProbability) {
            val c = fourFloor
            fourFloor = 0
            if (pool.upFloor == null) CardSettle(2, c, pool, false, isUp = false)
            else if (upFour) {
                upFour = false
                CardSettle(3, c, pool, isFloor = false, isUp = true)
            } else {
                val rr = Math.random()
                if (rr > pool.upFloor!!) {
                    upFour = true
                    CardSettle(2, c, pool, isFloor = false, isUp = false)
                } else CardSettle(3, c, pool, isFloor = false, isUp = false)
            }
        } else CardSettle(1, 0, pool, false, isUp = false)
    }

}