package com.shik2.starter;

import com.shik2.ResCompleteRate;
import com.shik2.ResTotalDelay;
import com.shik2.Statistics;
import com.shik2.compare.MinAlgorithm;
import com.shik2.earliest.Task;
import com.shik2.ga.MEC;
import com.shik2.input.InputVal;
import com.shik2.utils.DataProcess;
import com.shik2.utils.DeepCopy;
import com.shik2.utils.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shik2
 * @date 2020/12/29
 * <p>
 * Description:
 **/
public class StarterMin {
    static double currTime = 0;
    private static Logger logger = LoggerFactory.getLogger(StarterMin.class);

    public static void main(String[] args) {
        FileHelper taskFile = new FileHelper(DataProcess.TASKLISTPATH);
        List<InputVal> inputVals = taskFile.getObjFromFile();

        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();

        List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果

        for (int m = 0; m < inputVals.size(); m++) {
            logger.info("lamda: " + inputVals.get(m).getLamda());
            ResCompleteRate rcr = new ResCompleteRate(inputVals.get(m).getLamda());
            ResTotalDelay rtd = new ResTotalDelay(inputVals.get(m).getLamda());

            logger.info("==============================min-min调度====================================");
            StarterMin.minSchedule(inputVals, m, mecList, rcr, rtd);


            // 记录结果
            resCRList.add(rcr);
            resTDList.add(rtd);

        }

        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info(resCompleteRate.getMinCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info(resTotalDelay.getMinTDelay() + "");
        }
    }


    /**
     * min-min 调度
     *
     * @param inputVals 任务总集
     * @param m         第m个任务列表
     * @param rcr       结果保存对象
     */
    public static void minSchedule(List<InputVal> inputVals, int m, List<MEC> oriMecList, ResCompleteRate rcr, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);

        Statistics minSts = new Statistics();
        int totalTaskNum = taskList.size(), finish = 0;
        double calMount = 0;
        int task45_5 = 0, task4_45 = 0;
        //调度开始
        while (taskList.size() > 0) {
            // 时间流逝
            if (!(taskList.get(0).getArriveTime() > currTime)) {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    for (MEC mec : mecList) {
                        mec.removeCompletedTasks(currTime, minSts);
                    }
                    Task taskNow = taskList.get(0);
                    if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                        minSts.urgencyTask++;
                    }
                    // 找到最早完成的MEC
                    MEC minMEC = null;
                    double earlyTime = Double.MAX_VALUE;
//                    System.out.println("当前任务" + taskNow);
                    for (int i = 0; i < mecList.size(); i++) {
//                        System.out.println("MEC:" + mecList.get(i).getId() + " c: " + mecList.get(i).getC());
//                        System.out.println(mecList.get(i).getTaskList());
                        if (MinAlgorithm.canAcceptTask(mecList.get(i), taskNow, currTime)) {
                            double tmpTime = MinAlgorithm.calFinishTime(mecList.get(i), taskNow, currTime);
//                            System.out.println("MEC可用，任务完成时间：" + DataProcess.format(tmpTime, 2));
                            if (tmpTime < earlyTime) {
                                earlyTime = tmpTime;
                                minMEC = mecList.get(i);
                            }
                        }
                    }
                    if (minMEC != null) {
                        finish++;
                        taskNow.setStartTime(MinAlgorithm.calStartTime(minMEC, currTime));
//                        System.out.println("选择MEC" + minMEC.getId());
//                        System.out.println(minMEC.getTaskList());
                        minMEC.addTask(taskNow);
//                        System.out.println(minMEC.getTaskList());
                        if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                            minSts.finishUrgencyTask++;
                            calMount += taskNow.getL();
                        }
                        if (taskNow.getL() >= 4.5) {
                            task45_5++;
                        } else if (taskNow.getL() >= 4) {
                            task4_45++;
                        }
                    }
                    taskList.remove(0);
                }
            }
            timeFlies();    // 时间流逝
            continue;
        }


        while (!isAllTaskFinish(mecList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : mecList) {
                mec.removeCompletedTasks(currTime, minSts);
            }
            timeFlies();
        }

        double cRate = DataProcess.format(minSts.finishUrgencyTask * 100 / (double) minSts.urgencyTask, 2);
        double tDelay = DataProcess.format(minSts.totalNormalDelay / minSts.normalTask, 2);

        logger.info("完成紧急任务数：" + minSts.finishUrgencyTask);
        logger.info("总共紧急任务数：" + minSts.urgencyTask);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalTaskNum);
        logger.info("移除普通任务数：" + minSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rcr.setMinCRate(cRate);
        rtd.setMinTDelay(tDelay);
    }

    public static void timeFlies() {
        currTime = DataProcess.format(currTime + 0.01, 2);
    }


    public static boolean isAllTaskFinish(List<MEC> mecList) {
        for (MEC mec : mecList) {
            if (mec.getTaskList().size() > 0) {
                return false;
            }
        }
        return true;
    }
}
