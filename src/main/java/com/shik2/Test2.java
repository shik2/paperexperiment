package com.shik2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shik2
 * @date 2020/12/21
 * <p>
 * Description:
 **/
public class Test2 {
    public static void main(String[] args) {
//        for (int i = 0; i < 100; i++) {
//            System.out.println(format(Math.random() * 5,3));   // 随机产生任务计算量（1 ~ 5s）);
//        }
       List<Integer> list = new ArrayList<>();
       list.add(0);
       list.add(1);
       list.add(2);

       list.add(1,3);
        for (Integer integer : list) {
            System.out.println(integer);
        }

    }

    // 规范化小数
    public static double format(double number, int n) {
        BigDecimal bd = new BigDecimal(number);
        BigDecimal bd1 = bd.setScale(n, bd.ROUND_HALF_UP);
        return bd1.doubleValue();
    }
}
