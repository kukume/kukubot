package me.kuku.yuq.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.abs;

/**
 * @author SitaNya
 * 日期: 2019-06-15
 * 电子邮箱: sitanya@qq.com
 * 维护群(QQ): 162279609
 * 有任何问题欢迎咨询
 * 类说明: 今日人品类，其实不是很想做……
 */
public class Jrrp  {


    /**
     * 将系统信息Date转化为毫秒时间戳字符串
     *
     * @param date 系统日期
     * @return 系统日期的毫秒时间戳
     */
    private static String toTimestamp(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String result;
        result = df.format(date);
        return result;
    }

    /**
     * 将结果发送出去，里面使用了对方的QQ号和时间戳作为种子
     */
    public static int get(long qq)  {

        String date = toTimestamp(new Date());
        long tmp = 1;
        char[] b = (String.valueOf(qq) + date).toCharArray();
        //转换成响应的ASCLL
        for (char c : b) {
            tmp *= c;
        }
        long result = abs(tmp % 99);
        MersenneTwister mersenneTwister = new MersenneTwister(result + 1);
        int x = mersenneTwister.nextInt(100 - 1 + 1) + 1;
        return  x;
    }


}
