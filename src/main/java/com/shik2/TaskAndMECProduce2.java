package com.shik2;

import com.shik2.earliest.Task;
import com.shik2.ga.MEC;
import com.shik2.input.InputVal;
import com.shik2.utils.DataProcess;
import com.shik2.utils.FileHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: shik2
 * @date: 2020/12/18 0018 15:36
 * <p>
 * Description: 生成不同数量的MECList
 */
public class TaskAndMECProduce2 {

    static int mecNum = 30;

    public static void main(String[] args) {
        // 产生一定量的MEC,并持久化
        List<List<MEC>> MECLists = new ArrayList<>();
        for (int i = 10; i <= 60; i+=2) {
            MECLists.add(generateMECs(i));
        }
        // 持久化MEC列表到磁盘
        FileHelper fileHelper2 = new FileHelper(DataProcess.MECLISTPATH2);
        fileHelper2.saveObjToFile(MECLists);

        // 产生任务测试集
        List<InputVal> inputVals = new ArrayList<>();
        List<Task> tasks = generateTasks(16, 1500);
        inputVals.add(new InputVal(16, tasks));
        // 持久化任务集合
        FileHelper fileHelper = new FileHelper(DataProcess.TASKLISTPATH2);
        fileHelper.saveObjToFile(inputVals);
    }


    public static List<Task> generateTasks(double lamda, int taskNum) {
        List<Task> taskList = new ArrayList<>();
//        double lamda = 15;   // 每个MEC单位时间内产生的任务数量
        double currTime = 0;
        for (int i = 0; i < taskNum; i++) {
            double ugc = Math.random();
            int urgency = ugc < 0.6 ? 1 : (ugc < 0.8) ? 2 : 3;    // 随机产生任务等级（1、2、3）
            double l = format(Math.random() * 4 + 1, 3);   // 随机产生任务计算量（1 ~ 5s）
            double dori = format(currTime + l + Math.random() * 2 + 2, 2);    // 任务默认截止时间为计算时间的1.5倍
            Task newTask = new Task(l, urgency, dori);
            // 计算dl
            double x1 = Math.random() * 60 + 30;
            int x2 = (int) (Math.random() * 3) + 1;
            int x3 = (int) (Math.random() * 30) + 1;
            int x4 = (int) (Math.random() * 3) + 1;
            double dl, dl_closest = 0;
            if (urgency == 1 || urgency == 2) {
                dl = dori + 3 * (urgency - 1) - 0.05 * (x1 - 60) - 0.5 * (x2 - 1) - 0.5 * (x4 - 1);
            } else {
                dl = dori + 3 * (urgency - 1) - 0.1 * (x3 - 15) + 0.1 * (x4 - 1);
//                dl = Double.MAX_VALUE;
            }
            int closestMEC = (int) (Math.random() * mecNum);
            dl = format(dl, 2);
            newTask.setDeadline(format(dl, 2));
            newTask.setId(i);
            newTask.setArriveTime(format(currTime, 2));
            newTask.setClosestMEC(closestMEC);
            newTask.setDlClosest(dl_closest);
            taskList.add(newTask);

            currTime += randExp(lamda);
        }

        return taskList;
    }

    public static List<MEC> generateMECs(int MECNum) {
        // 初始化30个MEC
        List<MEC> mecList = new ArrayList<>();
        for (int i = 0; i < MECNum; i++) {
            mecList.add(new MEC(i, format(Math.random() + 0.5, 2)));  // 计算能力从0.5 ~ 1.5的ECS
        }
        return mecList;
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
        t = Math.random();
        temp = -(1 / lamda) * Math.log(t);
        temp = format(temp, 2);
        return temp;
    }


}
