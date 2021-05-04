package com.shik2.starter;

import com.shik2.ResCompleteRate;
import com.shik2.ResTotalDelay;
import com.shik2.Statistics;
import com.shik2.compare.FirstFitAlgorithm;
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
public class StarterEDF {
    static double currTime = 0;
    private static Logger logger = LoggerFactory.getLogger(StarterEDF.class);

    public static void main(String[] args) {
        List<ResCompleteRate> resList = new ArrayList<>();  // 存放结果
        FileHelper taskFile = new FileHelper(DataProcess.TASKLISTPATH);
        List<InputVal> inputVals = taskFile.getObjFromFile();

        for (InputVal inputVal : inputVals) {
            System.out.println(inputVal);
        }
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();

        List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果

        for (int m = 0; m < inputVals.size(); m++) {
            logger.info("lamda: " + inputVals.get(m).getLamda());
            ResCompleteRate rcr = new ResCompleteRate(inputVals.get(m).getLamda());
            ResTotalDelay rtd = new ResTotalDelay(inputVals.get(m).getLamda());
            System.out.println(inputVals.get(m).getLamda());
            StarterEDF.firstFitSchdule(inputVals, m, mecList, rcr, rtd);

            // 记录结果
            resCRList.add(rcr);
            resTDList.add(rtd);

        }

        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info(resCompleteRate.getEdfCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info(resTotalDelay.getEdfTDelay() + "");
        }
    }

    /**
     * 最先满足要求调度
     *
     * @param inputVals 输入的任务总集
     * @param m         第m个任务列表
     * @param rs        结果保存对象
     */
    public static void firstFitSchdule(List<InputVal> inputVals, int m, List<MEC> oriMecList, ResCompleteRate rs, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);

        Statistics edfSts = new Statistics();
        int acceptTaskNum = 0, urgencyTaskNum = 0, totalTaskNum = taskList.size(), finish = 0;
        double calMount = 0;
        int task45_5 = 0, task4_45 = 0;
        //调度开始
        while (taskList.size() > 0) {
            // 时间流逝
            if (!(taskList.get(0).getArriveTime() > currTime)) {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    for (MEC mec : mecList) {
                        mec.removeCompletedTasks(currTime, edfSts);
                    }
                    Task taskNow = taskList.get(0);
//                    logger.info("当前任务:" + taskNow);
                    if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                        edfSts.urgencyTask++;
                        for (int i = 0; i < mecList.size(); i++) {
                            MEC checkMEC = mecList.get(i);
//                            logger.info("当前检查MEC" + checkMEC.getId() + " c=" + checkMEC.getC());
//                            logger.info("任务列表：" + checkMEC.getTaskList());
                            if (FirstFitAlgorithm.canAcceptTask(checkMEC, taskNow, currTime)) {
                                taskNow.setStartTime(FirstFitAlgorithm.calStartTime(checkMEC, currTime));
                                checkMEC.addTask(taskNow);
                                finish++;
//                            logger.info("MEC可用，任务加入后的列表：" + checkMEC.getTaskList());
                                edfSts.finishUrgencyTask++;
                                calMount += taskNow.getL();
                                if (taskNow.getL() >= 4.5) {
                                    task45_5++;
                                } else if (taskNow.getL() >= 4) {
                                    task4_45++;
                                }
                                break;
                            }
                        }
                    } else {
                        int idx = (int) (Math.random() * mecList.size());
                        taskNow.setStartTime(FirstFitAlgorithm.calStartTime(mecList.get(idx), currTime));
                        mecList.get(idx).addTask(taskNow);
                        finish++;
                    }
                    // 找到第一个满足的MEC

                    taskList.remove(0);
                }
            }
            timeFlies();    // 时间流逝
            continue;
        }


        while (!isAllTaskFinish(mecList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : mecList) {
                mec.removeCompletedTasks(currTime, edfSts);
            }
            timeFlies();
        }

        double cRate = DataProcess.format(edfSts.finishUrgencyTask * 100 / (double) edfSts.urgencyTask, 2);
        double tDelay = DataProcess.format(edfSts.totalNormalDelay / edfSts.normalTask, 2);

        logger.info("完成紧急任务数：" + edfSts.finishUrgencyTask);
        logger.info("总共紧急任务数：" + edfSts.urgencyTask);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalTaskNum);
        logger.info("移除普通任务数：" + edfSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rs.setEdfCRate(cRate);
        rtd.setEdfTDelay(tDelay);
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
