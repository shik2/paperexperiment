package com.shik2;

import com.shik2.earliest.Task;
import com.shik2.ga.Individual;
import com.shik2.ga.MEC;
import com.shik2.utils.FileHelper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * @author shik2
 * @date 2020/12/18
 * <p>
 * Description:
 **/
public class Test {

    static double currTime = 0;

    public static void main(String[] args) {
        // 获取任务集和MEC集
        FileHelper taskFile = new FileHelper("C:\\Users\\Administrator\\Desktop\\taskList.txt");
        List<Task> taskList = taskFile.getObjFromFile();
        FileHelper mecFile = new FileHelper("C:\\Users\\Administrator\\Desktop\\mecList.txt");
        List<MEC> mecList = mecFile.getObjFromFile();
        Collections.sort(mecList, (t1, t2) -> t1.getC() - t2.getC() < 0 ? 1 : (t1.getC() - t2.getC() == 0) ? 0 : -1);
        for (Task task : taskList) {
            System.out.println("任务" + task.getId() + "--紧急度：" + task.getUrgency() + " 到达时间：" + task.getArriveTime() + " 截止时间：" + task.getDeadline() + " 任务量：" + task.getL());
        }
    }


    public static void timeFlies() {
        currTime = format(currTime + 0.01, 2);
    }

    // 规范化小数
    public static double format(double number, int n) {
        BigDecimal bd = new BigDecimal(number);
        BigDecimal bd1 = bd.setScale(n, bd.ROUND_HALF_UP);
        return bd1.doubleValue();
    }
}
