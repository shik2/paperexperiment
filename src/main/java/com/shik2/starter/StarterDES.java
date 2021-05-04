package com.shik2.starter;

import com.shik2.ResCompleteRate;
import com.shik2.ResTotalDelay;
import com.shik2.Statistics;
import com.shik2.compare.FirstFitAlgorithm;
import com.shik2.earliest.DESAlgorithm;
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

import static com.shik2.TaskAndMECProduce.generateTasks;

/**
 * @author shik2
 * @date 2020/12/30
 * <p>
 * Description:
 **/
public class StarterDES {

    static double currTime = 0;
    private static final Logger logger = LoggerFactory.getLogger(StarterDES.class);

    public static void main(String[] args) {

        List<List<ResCompleteRate>> lists1 = new ArrayList<>();
        List<List<ResTotalDelay>> lists2 = new ArrayList<>();

//        for (int i = 0; i < 20; i++) {
            /*FileHelper taskFile = new FileHelper(DataProcess.TASKLISTPATH2);
            List<InputVal> inputVals = taskFile.getObjFromFile();*/

        List<InputVal> inputVals = new ArrayList<>();
        for (int lamda = 1000; lamda <= 1000; lamda += 2) {
            List<Task> tasks = generateTasks(lamda, 5000);
            inputVals.add(new InputVal(lamda, tasks));
        }

        for (InputVal inputVal : inputVals) {
            System.out.println(inputVal);
        }

        List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果

        for (int m = 0; m < inputVals.size(); m++) {
            logger.info("lamda: " + inputVals.get(m).getLamda());
            ResCompleteRate rs = new ResCompleteRate(inputVals.get(m).getLamda());
            ResTotalDelay rtd = new ResTotalDelay(inputVals.get(m).getLamda());
            StarterDES.desSchedule(inputVals, m, rs, rtd);
            // 记录结果
            resCRList.add(rs);
            resTDList.add(rtd);

        }

        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info(resCompleteRate.getDesCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info(resTotalDelay.getDesTDelay() + "");
        }
//            lists1.add(resCRList);
//            lists2.add(resTDList);
//        }
//        logger.info("------------");
//        for (int i = 0; i < lists1.get(0).size(); i++) {
//            double cSum = 0;
//            for (int j = 0; j < lists1.size(); j++) {
//                cSum += lists1.get(j).get(i).getDesCRate();
//            }
//            logger.info(cSum / 20 + "");
//        }
//        logger.info("------------");
//        for (int i = 0; i < lists1.get(0).size(); i++) {
//            double dSum = 0;
//            for (int j = 0; j < lists1.size(); j++) {
//                dSum += lists2.get(j).get(i).getDesTDelay();
//            }
//            logger.info(dSum / 20 + "");
//        }


    }

    public static void desSchedule(List<InputVal> inputVals, int m, ResCompleteRate rs, ResTotalDelay rtd) {
        currTime = 0;
        // 获取任务集和MEC集
        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();

        Statistics desSts = new Statistics();
        double calMount = 0, finish = 0;
        int task45_5 = 0, task4_45 = 0, totalTaskNum = taskList.size();
        //调度开始
        while (taskList.size() > 0) {
            if (taskList.get(0).getArriveTime() > currTime) {
                timeFlies();    // 时间流逝
                continue;
            } else {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    // 移除所有MEC中已经完成的任务
                    for (MEC mec : mecList) {
                        mec.removeCompletedTasks(currTime, desSts);
                    }
                    Task taskNow = taskList.get(0);
                    if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                        desSts.urgencyTask++;
                    }
                    // 选取最优的MEC
                    MEC bestMEC = DESAlgorithm.getBestMEC(mecList, taskNow, currTime);
                    if (bestMEC != null) {
                        bestMEC.acceptAndUpdateTime(taskNow, currTime);
                        finish++;
                        calMount += taskNow.getL();
                        if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                            desSts.finishUrgencyTask++;
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
                mec.removeCompletedTasks(currTime, desSts);
            }
            timeFlies();
        }

        double cRate = DataProcess.format(desSts.finishUrgencyTask * 100 / (double) desSts.urgencyTask, 2);
        double tDelay = DataProcess.format(desSts.totalNormalDelay / desSts.normalTask, 2);

        logger.info("完成紧急任务数：" + desSts.finishUrgencyTask);
        logger.info("总共紧急任务数：" + desSts.urgencyTask);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalTaskNum);
        logger.info("移除普通任务数：" + desSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rs.setDesCRate(cRate);
        rtd.setDesTDelay(tDelay);
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