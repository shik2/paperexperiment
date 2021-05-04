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
 * @date 2020/12/28
 * <p>
 * Description: 任务的请求密度对紧急任务完成率的影响
 **/
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果
        //读取任务列表和MEC列表
        FileHelper taskFile = new FileHelper(DataProcess.TASKLISTPATH);
        List<InputVal> inputVals = taskFile.getObjFromFile();
        for (InputVal inputVal : inputVals) {
            System.out.println(inputVal);
        }
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();
        System.out.println(mecList.size());
        for (int m = 0; m < inputVals.size(); m++) {
            logger.info("lamda: " + inputVals.get(m).getLamda());
            List<Task> tasks = inputVals.get(m).getTaskList();
            int urgencyTask = 0, normalTask = 0;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getUrgency() == 3) {
                    normalTask++;
                } else {
                    urgencyTask++;
                }
            }
            logger.info("紧急任务数：" + urgencyTask + " 普通任务数：" + normalTask);
            ResCompleteRate rcr = new ResCompleteRate(inputVals.get(m).getLamda());
            ResTotalDelay rtd = new ResTotalDelay(inputVals.get(m).getLamda());

            logger.info("==============================本文调度算法=====================================");
            StarterPaper.paperSchedule(inputVals, m, mecList, rcr, rtd);

            logger.info("==============================最近调度算法====================================");
            StarterClosest.closestSchdule(inputVals, m, mecList, rcr, rtd);

            logger.info("==============================完全遗传算法====================================");
            StarterGA.gaSchedule(inputVals, m, mecList, rcr, rtd);
            // EDF
            logger.info("==============================最先满足要求调度====================================");
            StarterEDF.firstFitSchdule(inputVals, m, mecList, rcr, rtd);

            // min-min
            logger.info("==============================min-min调度====================================");
            StarterMin.minSchedule(inputVals, m, mecList, rcr, rtd);

            //  DES
//            logger.info("==============================DES调度====================================");
//            StarterDES.desSchedule(inputVals, m, rcr, rtd);

            // 记录结果
            resCRList.add(rcr);
            resTDList.add(rtd);
        }

        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info("papaerCR = " + resCompleteRate.getPaperCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info("closeCR = " + resCompleteRate.getCloseCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info("gaCR = " + resCompleteRate.getGaCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info("EDFCR = " + resCompleteRate.getEdfCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info("minCR = " + resCompleteRate.getMinCRate() + "");
        }
//        logger.info("-------------------------------------------------");
//        for (ResCompleteRate resCompleteRate : resCRList) {
//            logger.info("DESCR = " + resCompleteRate.getDesCRate() + "");
//        }


        logger.info("====================================================");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info("papaerTD = " + resTotalDelay.getPaperTDelay() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info("closeTD = " + resTotalDelay.getCloseTDelay() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info("gaTD = " + resTotalDelay.getGaTDelay() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info("EDFTD = " + resTotalDelay.getEdfTDelay() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info("minTD = " + resTotalDelay.getMinTDelay() + "");
        }
//        logger.info("-------------------------------------------------");
//        for (ResTotalDelay resTotalDelay : resTDList) {
//            logger.info("DESTD = " + resTotalDelay.getDesTDelay() + "");
//        }

    }

}
