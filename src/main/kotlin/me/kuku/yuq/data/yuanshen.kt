package me.kuku.yuq.data

interface YuanShenPool : CardPool {

    val upFive: LevelPrizePool
    val normalFive: LevelPrizePool
    val upFour: LevelPrizePool
    val normalFour: LevelPrizePool
    val rubbish: LevelPrizePool
        get() = arrayOf("弹弓", "神射手之誓", "鸦羽弓", "翡玉法球", "讨龙英杰谭", "魔导绪论", "黑缨枪", "以理服人", "沐浴龙血的剑", "铁影阔剑", "飞天御剑", "黎明神剑", "冷刃")

    val fiveFloor: Int
        get() = 90
    val fourFloor: Int
        get() = 10
    val upFloor: Double?
        get() = null

    val fiveProbability
        get() = 0.006
    val fourProbability
        get() = 0.051

    override operator fun invoke(result: CardSettle): String {
        val level = result.level
        val pp = when (level) {
            5 -> upFive
            4 -> normalFive
            3 -> upFour
            2 -> normalFour
            else -> rubbish
        }()
        return with(result) {
            when {
                level > 3 -> "--------------------\n" +
                        "|| 金: $pp (${if (isFloor) "保底" else count})${if (isUp) " (大保底)" else ""}\n" +
                        "--------------------"
                level > 1 -> "++ 紫: $pp (${if (isFloor) "保底" else count})${if (isUp) " (大保底)" else ""} "
                else -> "-- 蓝: $pp"
            }
        }
    }

    object NormalPool : YuanShenPool {
        override val name = "原神"

        override val upFive = arrayOf("")
        override val upFour = arrayOf("")

        override val normalFive = arrayOf("刻晴", "莫娜", "七七", "迪卢克", "琴", "阿莫斯之弓", "天空之翼", "四风原典", "天空之卷", "和璞鸢", "天空之脊", "狼的末路", "天空之傲", "天空之刃", "风鹰剑")
        override val normalFour = arrayOf("砂糖", "重云", "诺艾尔", "班尼特", "菲谢尔", "凝光", "行秋", "北斗", "香菱", "安柏", "雷泽", "凯亚", "芭芭拉", "丽莎", "弓藏", "祭礼弓", "绝弦", "西风猎弓", "昭心", "祭礼残章", "流浪乐章", "西风秘典", "西风长枪", "匣里灭辰", "雨裁", "祭礼大剑", "钟剑", "西风大剑", "匣里龙吟", "祭礼剑", "笛剑", "西风剑")
    }

    object KeLiPool : YuanShenPool {
        override val name = "可莉"

        override val upFive = arrayOf("可莉")
        override val normalFive = arrayOf("刻晴", "莫娜", "七七", "迪卢克", "琴")
        override val upFour = arrayOf("砂糖", "行秋", "诺艾尔")
        override val normalFour = arrayOf("重云", "班尼特", "菲谢尔", "凝光", "北斗", "香菱", "雷泽", "芭芭拉", "弓藏", "祭礼弓", "绝弦", "西风猎弓", "昭心", "祭礼残章", "流浪乐章", "西风秘典", "西风长枪", "匣里灭辰", "雨裁", "祭礼大剑", "钟剑", "西风大剑", "匣里龙吟", "祭礼剑", "笛剑", "西风剑")

        override val upFloor = 0.5
    }

    object LangMoPool : YuanShenPool {
        override val name = "狼末"

        override val upFive = arrayOf("四风原典", "狼的末路")
        override val normalFive = arrayOf("阿莫斯之弓", "天空之翼", "天空之卷", "天空之脊", "天空之傲", "天空之刃", "风鹰剑")
        override val upFour = arrayOf("祭礼弓", "祭礼残章", "匣里灭辰", "祭礼大剑", "祭礼剑")
        override val normalFour = arrayOf("砂糖", "重云", "诺艾尔", "班尼特", "菲谢尔", "凝光", "行秋", "北斗", "香菱", "雷泽", "芭芭拉", "弓藏", "绝弦", "西风猎弓", "昭心", "流浪乐章", "西风秘典", "西风长枪", "雨裁", "钟剑", "西风大剑", "匣里龙吟", "笛剑", "西风剑")

        override val upFloor = 0.75
        override val fiveFloor = 80

        override val fiveProbability = 0.007
        override val fourProbability = 0.06
    }
}

object YuanShenPools : MutableMap<String, YuanShenPool> by HashMap() {

    init {
        register(
                YuanShenPool.NormalPool,
                YuanShenPool.KeLiPool,
                YuanShenPool.LangMoPool,
        )
    }

    private fun register(vararg pools: YuanShenPool) {
        for (pool in pools) {
            put(pool.name, pool)
        }
    }


}

