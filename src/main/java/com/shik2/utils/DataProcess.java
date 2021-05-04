package com.shik2.utils;

import java.math.BigDecimal;

/**
 * @author: shik2
 * @date: 2020/12/23 0023 15:19
 *
 * Description: 数据处理工具类
 *
 */
public class DataProcess {

    public static final String TASKLISTPATH = "C:\\Users\\Administrator\\Desktop\\taskList.txt";
    public static final String TASKLISTPATH2 = "C:\\Users\\Administrator\\Desktop\\taskList2.txt";
    public static final String MECLISTPATH = "C:\\Users\\Administrator\\Desktop\\mecList.txt";
    public static final String MECLISTPATH2 = "C:\\Users\\Administrator\\Desktop\\mecList2.txt";

    /**
     * 规范化小数
     * @param number
     * @param n
     * @return
     */
    public static double format(double number, int n) {
        BigDecimal bd = new BigDecimal(number);
        BigDecimal bd1 = bd.setScale(n, bd.ROUND_HALF_UP);
        return bd1.doubleValue();
    }
}
