package me.kuku.yuq.data

typealias LevelPrizePool = Array<String>

operator fun LevelPrizePool.invoke() = get((Math.random() * size).toInt())

data class CardSettle(
    val level: Int,
    val count: Int,
    val pool: CardPool,
    val isFloor: Boolean,
    val isUp: Boolean,
)

interface CardPool{
    val name: String

    operator fun invoke(result: CardSettle): String
}