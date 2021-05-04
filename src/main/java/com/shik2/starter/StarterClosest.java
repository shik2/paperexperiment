package com.shik2.starter;

import com.shik2.ResCompleteRate;
import com.shik2.ResTotalDelay;
import com.shik2.Statistics;
import com.shik2.compare.CloseAlgorithm;
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
 * @date 2020/12/30
 * <p>
 * Description:
 **/
public class StarterClosest {
    static double currTime = 0;
    private static Logger logger = LoggerFactory.getLogger(StarterClosest.class);


    public static void main(String[] args) {
        FileHelper taskFile = new FileHelper(DataProcess.TASKLISTPATH);
        List<InputVal> inputVals = taskFile.getObjFromFile();

        for (InputVal inputVal : inputVals) {
            System.out.println(inputVal);
            for (Task task : inputVal.getTaskList()) {
                if(task.getUrgency()==3)
                    System.out.println(task.getDeadline());
            }
        }
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();


        List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果

        for (int m = 0; m < inputVals.size(); m++) {
            logger.info("lamda: " + inputVals.get(m).getLamda());
            ResCompleteRate rcr = new ResCompleteRate(inputVals.get(m).getLamda());
            ResTotalDelay rtd = new ResTotalDelay(inputVals.get(m).getLamda());
            logger.info("==============================cloest调度====================================");
            StarterClosest.closestSchdule(inputVals, m, mecList, rcr, rtd);
            // 记录结果
            resCRList.add(rcr);
            resTDList.add(rtd);
        }
        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info(resCompleteRate.getCloseCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info(resTotalDelay.getCloseTDelay() + "");
        }
    }


    /**
     * 直接卸载到最近的MEC
     *
     * @param inputVals 输入的任务总集
     * @param m         第m个任务列表
     * @param rcr       结果保存对象
     */
    public static void closestSchdule(List<InputVal> inputVals, int m, List<MEC> oriMecList, ResCompleteRate rcr, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);
        Statistics closeSts = new Statistics();
        int  totalTask = taskList.size();
        //调度开始
        while (taskList.size() > 0) {
            // 时间流逝
            if (!(taskList.get(0).getArriveTime() > currTime)) {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    for (MEC mec : mecList) {
                        mec.removeCompletedTasks(currTime, closeSts);
                    }
                    Task taskNow = taskList.get(0);
                    // 最近的MEC
                    MEC closestMEC = mecList.get(taskNow.getClosestMEC());
                    if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                        closeSts.urgencyTask++;
                    }else{
                        taskNow.setDeadline(Double.MAX_VALUE);
                    }
                    if (CloseAlgorithm.acceptTask(closestMEC, taskNow, currTime)) {
                        if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                            closeSts.finishUrgencyTask++;
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
                mec.removeCompletedTasks(currTime, closeSts);
            }
            timeFlies();
        }

        double cRate = DataProcess.format(closeSts.finishUrgencyTask * 100 / (double) closeSts.urgencyTask, 2);
        double tDelay = DataProcess.format(closeSts.totalNormalDelay / closeSts.normalTask, 2);

        System.out.println(closeSts.totalNormalDelay);

        logger.info("完成紧急任务数：" + closeSts.finishUrgencyTask);
        logger.info("总共紧急任务数：" + closeSts.urgencyTask);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalTask);
        logger.info("移除普通任务数：" + closeSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rcr.setCloseCRate(cRate);
        rtd.setCloseTDelay(tDelay);

    }

    public static void closestSchdule2(List<InputVal> inputVals, int m, List<MEC> oriMecList, ResCompleteRate rcr, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);
        Statistics closeSts = new Statistics();
        double calMount = 0;
        int task45_5 = 0, task4_45 = 0, totalTask = taskList.size();
        //调度开始
        while (taskList.size() > 0) {
            // 时间流逝
            if (!(taskList.get(0).getArriveTime() > currTime)) {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    for (MEC mec : mecList) {
                        mec.removeCompletedTasks(currTime, closeSts);
                    }
                    Task taskNow = taskList.get(0);
                    // 最近的MEC
                    int closestMECIdx = (int) (Math.random() * mecList.size());
                    MEC closestMEC = mecList.get(closestMECIdx);
                    if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                        closeSts.urgencyTask++;
                    }else {
                        taskNow.setDeadline(Double.MAX_VALUE);
                    }
                    if (CloseAlgorithm.acceptTask(closestMEC, taskNow, currTime)) {
                        if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                            closeSts.finishUrgencyTask++;
                            calMount += taskNow.getL();
                            if (taskNow.getL() >= 4.5) {
                                task45_5++;
                            } else if (taskNow.getL() >= 4) {
                                task4_45++;
                            }
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
                mec.removeCompletedTasks(currTime, closeSts);
            }
            timeFlies();
        }

        double cRate = DataProcess.format(closeSts.finishUrgencyTask * 100 / (double) closeSts.urgencyTask, 2);
        double tDelay = DataProcess.format(closeSts.totalNormalDelay / closeSts.normalTask, 2);

        logger.info("完成紧急任务数：" + closeSts.finishUrgencyTask);
        logger.info("总共紧急任务数：" + closeSts.urgencyTask);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalTask);
        logger.info("移除普通任务数：" + closeSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rcr.setCloseCRate(cRate);
        rtd.setCloseTDelay(tDelay);

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
