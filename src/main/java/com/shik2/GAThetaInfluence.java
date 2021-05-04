package com.shik2;

import com.shik2.earliest.Task;
import com.shik2.ga.MEC;
import com.shik2.input.InputVal;
import com.shik2.starter.*;
import com.shik2.utils.DataProcess;
import com.shik2.utils.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shik2
 * @date 2021/01/07
 * <p>
 * Description:
 **/
public class GAThetaInfluence {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果
        List<Double> aTimeList = new ArrayList<>();         //算法执行时间
        List<Double> gaTimeList = new ArrayList<>();         //算法执行时间

        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();
        List<Task> tasks = TaskAndMECProduce.generateTasks(24, 1000);

        List<InputVal> inputVals = new ArrayList<>();
        inputVals.add(new InputVal(21, tasks));
        int m = 0;
        ResCompleteRate rcr1 = new ResCompleteRate(20);
        ResTotalDelay rtd1 = new ResTotalDelay(20);

        logger.info("==============================最近调度算法====================================");
        StarterClosest.closestSchdule(inputVals, m, mecList, rcr1, rtd1);

//        logger.info("==============================完全遗传算法====================================");
//        StarterGA.gaSchedule(inputVals, m, mecList, rcr1, rtd1);
        // EDF
        logger.info("==============================最先满足要求调度====================================");
        StarterEDF.firstFitSchdule(inputVals, m, mecList, rcr1, rtd1);

        // min-min
        logger.info("==============================min-min调度====================================");
        StarterMin.minSchedule(inputVals, m, mecList, rcr1, rtd1);


        for (int gaTheta = 2; gaTheta <= 20; gaTheta += 2) {
            logger.info("gaTheta=" + gaTheta);

            int urgencyTask = 0, normalTask = 0;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getUrgency() == 3) {
                    normalTask++;
                } else {
                    urgencyTask++;
                }
            }
            logger.info("紧急任务数：" + urgencyTask + " 普通任务数：" + normalTask);
//
            ResCompleteRate rcr = new ResCompleteRate(15);
            ResTotalDelay rtd = new ResTotalDelay(15);
            long startTime = System.currentTimeMillis();
            logger.info("==============================本文调度算法=====================================");
            StarterPaper.paperScheduleOnDifferentGATheta(tasks, mecList, gaTheta, rcr, rtd, gaTimeList);
            long endTime = System.currentTimeMillis();
            double usedTime = (endTime - startTime) / 1000.0;

       /*     logger.info("==============================完全遗传算法====================================");
//            StarterGA.gaThetaSchedule(inputVals, lamdaIDX, gaTheta, mecList, rcr, rtd);
            StarterGA.ga(tasks, gaTheta, mecList, rcr, rtd);*/


            // 记录结果
            resCRList.add(rcr);
            resTDList.add(rtd);
            aTimeList.add(usedTime);
        }

        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info(resCompleteRate.getPaperCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info(resTotalDelay.getPaperTDelay() + "");
        }
        logger.info("-------------------------------------------------");
        for (Double usetime : aTimeList) {
            logger.info(usetime + "");
        }
        logger.info("-------------------------------------------------");
        for (Double usetime : gaTimeList) {
            logger.info(usetime + "");
        }
    }

}