package com.shik2.ga;

import com.shik2.earliest.Task;
import com.shik2.utils.DeepCopy;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: shik2
 * @date: 2020/12/16 0016 15:41
 * Description: 计算适应度
 */
public class FitnessCalc {

//    // 用于计算适应度的MEC列表
//    private static List<MEC> mecList = new ArrayList<>();
//    // 用于计算适应度的Task列表
//    private static List<Task> taskList = new ArrayList<>();

    public void setMecList(List<MEC> mecList) {
        mecList = mecList;
    }

    public void setTaskList(List<Task> taskList) {
        taskList = taskList;
    }

    /**
     * 计算个体的适应值
     *
     * @param individual 待计算个体
     * @return fitness 个体适应度
     */
    public static double getFitness(Individual individual, double currTime) {
        // 先做一次mecList的深拷贝，避免影响下一个个体的适应度计算
        List<MEC> copyMecList = DeepCopy.deepCopy(ScheduleData.mecList);

        double fitness = 0;
        // 计算时延总和
        for (int i = 0; i < individual.size(); i++) {
//            System.out.println("第" + i + "位置分配的MEC" + individual.getGene(i));
            // 当前任务分配的MEC
            MEC assignMEC = copyMecList.get(individual.getGene(i));
            // 待分配的任务
//            Task newTask = DeepCopy.deepCopy(ScheduleData.taskList.get(i));
            Task newTask =ScheduleData.taskList.get(i);

            fitness += assignMEC.calWaitTime(currTime) + assignMEC.calRunTime(newTask);
            newTask.setStartTime(currTime + assignMEC.calWaitTime(currTime));
            assignMEC.addTask(newTask);
        }
        return format((1 / fitness) * 100, 3);
    }


    // 规范化小数
    public static double format(double number, int n) {
        BigDecimal bd = new BigDecimal(number);
        BigDecimal bd1 = bd.setScale(n, bd.ROUND_HALF_UP);
        return bd1.doubleValue();
    }

}