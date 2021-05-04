package com.shik2.earliest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * @author shik2
 * @date 2020/11/23
 **/

public class Main {


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

    public static void main(String[] args) {
        List<ECS> ecsList = new ArrayList<>();
        int finishTaskNum = 0;
        for (int i = 0; i < 10; i++) {
            ecsList.add(new ECS(i, Math.random() + 0.5, 0));  // 共10个计算能力从0.5 ~ 1.5的ECS
        }

        double lamda = 2;
        CountDownLatch latch = new CountDownLatch(10);
        // 10个ECS按照泊松流的形式产生任务并调度执行
        for (int i = 0; i < 10; i++) {
            int ecsIndex = i;
            new Thread(() -> {
                int count = 0;
                double currTime = 0, sleepTime = 0;
                ECS localECS = ecsList.get(ecsIndex);
                while (count < 50) {
                    double ugc = Math.random();
                    int urgency = ugc < 0.33 ? 1 : (ugc < 0.66) ? 2 : 3;    // 随机产生任务等级（1、2、3）
                    double l = Math.random() * 5;   // 随机产生任务计算量（1 ~ 5s）
                    double dori = currTime + l * 1.5;    // 默认截止时间为计算时间+1
                    Task newTask = new Task(l, urgency, dori);
                    count++;
                    // 计算dl
                    double x1 = Math.random() * 60 + 30;
                    int x2 = (int) Math.random() * 5 + 1;
                    int x3 = (int) Math.random() * 30 + 1;
                    int x4 = (int) Math.random() * 5 + 1;
                    double dl = 0;
                    if (urgency == 1 || urgency == 2) {
                        dl = dori + 3 * (urgency - 1) - 0.05 * (x1 - 60) - 0.5 * (x2 - 1) - 0.5 * (x4 - 1);
                    } else if (urgency == 3) {
                        dl = dori + 3 * (urgency - 1) - 0.1 * (x3 - 15) + 0.1 * (x4 - 1);
                    } else {
                        System.out.println("---------紧急度生成异常---------");
                    }
                    dl = format(dl, 1);
                    newTask.setDeadline(dl);
                    newTask.setId(count);
//                    System.out.println("ECS" + ecsIndex + "生成新任务"+newTask.getId()+"，截止日期为：" + dl);

                    List<Task> tasks = localECS.getTaskList();
                    // 移除已经运行完的任务
                    synchronized (localECS) {
                        while (tasks.size() > 0 && tasks.get(0).getStartTime() + tasks.get(0).getL() / localECS.getC() < currTime) {
                            localECS.removeFirst();
                            // 这里做完成任务量的统计
                        }
                    }
                    if (localECS.localDES(newTask)) {
                        System.out.println(newTask.getId() + " 由本地ECS" + ecsIndex + "直接执行");
                    } else {
                        //其他ECS直接执行——ECS选择算法
                        List<ECS> S1 = new ArrayList<>();
                        List<ECS> S2 = new ArrayList<>();
                        for (int j = 0; j < ecsList.size() && j != ecsIndex; j++) {
                            if (ecsList.get(j).canLocalDES(newTask)) {
                                if (ecsList.get(j).findInsertIdx(newTask) == ecsList.get(j).getTaskList().size()) {
                                    // 插入位置在末尾
                                    S1.add(ecsList.get(j));
                                } else {
                                    // 插入位置在中间
                                    S2.add(ecsList.get(j));
                                }
                            }
                        }
                        if (S1.size() > 0) {    // EST First
                            int earlyId = S1.get(0).getId();
                            double earlyTime = S1.get(0).getST();
                            for (int j = 1; j < S1.size(); j++) {
                                if (S1.get(j).getST() < earlyTime) {
                                    earlyId = S1.get(j).getId();
                                    earlyTime = S1.get(j).getST();
                                }
                            }
                            ecsList.get(earlyId).localDES(newTask);
                            System.out.println(newTask.getId() + " 由其他ECS" + earlyId + "直接执行");
                        } else if (S2.size() > 0) {  // 最短可用空闲间隔优先(SSFI)
                            int minIntervalId = S2.get(0).getId();
                            double minInterval = S2.get(0).getSFI(newTask);
                            for (int j = 1; j < S2.size(); j++) {
                                if (S2.get(j).getSFI(newTask) < minInterval) {
                                    minIntervalId = S2.get(j).getId();
                                    minInterval = S2.get(j).getST();
                                }
                            }
                            ecsList.get(minIntervalId).localDES(newTask);
                            System.out.println(newTask.getId() + " 由其他ECS" + minIntervalId + "直接执行");
                        } else {
                            System.out.println(newTask.getId() + " 找不到可以运行任务的ECS");
                        }
                    }
                    sleepTime = randExp(lamda);
                    try {
                        TimeUnit.MILLISECONDS.sleep((int) sleepTime * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currTime += sleepTime;
                    ecsList.get(ecsIndex).setCurrTime(currTime);
                }
                latch.countDown();
            }, "ECS-" + i).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("执行完毕，继续执行主线程");
        for (int i = 0; i < 10; i++) {
            finishTaskNum += ecsList.get(i).getFinishTask();
        }
        System.out.println("总共完成" + finishTaskNum + "个任务");
    }
}
