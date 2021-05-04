package com.shik2.ga;

import com.shik2.earliest.Task;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试类
 *
 * @author lixiaolin
 * @createDate 2016-06-22 17:51
 */
public class MainTest {
    public static void main(String[] args) {
       /* // 初始化30个MEC
        List<MEC> mecList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            mecList.add(new MEC(i, format(Math.random() + 0.5, 2)));  // 计算能力从0.5 ~ 1.5的ECS
        }
        // 随机产生10个待分配任务
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double ugc = Math.random();
            int urgency = ugc < 0.33 ? 1 : (ugc < 0.66) ? 2 : 3;    // 随机产生任务等级（1、2、3）
            double l = format(Math.random() * 5, 2);   // 随机产生任务计算量（1 ~ 5s）
            double dori = 0 + l * 1.5;    // 默认截止时间为计算时间+1
            Task newTask = new Task(l, urgency, dori);
            tasks.add(newTask);
        }
        // 设置遗传算法调度器的参数
        ScheduleData.setMecList(mecList);
        ScheduleData.setTaskList(tasks);
        Individual.setDefaultGeneLength(tasks.size());

        for (MEC mec : mecList) {
//            System.out.println("MEC" + m++ + "计算能力为：" + mec.getC());
            System.out.println(mec.getC());
        }
        System.out.println("----------------");
        for (Task task : tasks) {
//            System.out.println("Task" + t++ + "任务量为：" + task.getL());
            System.out.println(task.getL());
        }


        long startTime = System.currentTimeMillis();

        double currTime = 0;

        // 初始化一个种群
        Population myPop = new Population(60, true, currTime);
        // 不段迭代，进行进化操作。 直到找到期望的基因序列
        int generationCount = 0;
        System.out.println("初始种群适应度最佳为： " + myPop.getFittest().getFitness());
        while (generationCount < 50) {
            generationCount++;
            myPop = Algorithm.evolvePopulation(myPop, currTime);
            System.out.println("第" + generationCount + "次进化，种群最佳适应度为： " + myPop.getFittest().getFitness());
        }
        long endTime = System.currentTimeMillis();
        System.out.println("进化结束!");
        System.out.println("总共进化" + generationCount + "次，用时 " + (endTime - startTime) + "毫秒");
        System.out.println("最佳调度策略：" + myPop.getFittest());*/
    }



    // 规范化小数
    public static double format(double number, int n) {
        BigDecimal bd = new BigDecimal(number);
        BigDecimal bd1 = bd.setScale(n, bd.ROUND_HALF_UP);
        return bd1.doubleValue();
    }

    // 产生服从指数分布的随机数
    public static double randExp(double lamda) {
        double t, temp;
        t = (double) Math.random();
        temp = (double) (-(1 / lamda) * Math.log(t));
        temp = format(temp, 2);
        return temp;
    }
}
