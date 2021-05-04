package com.shik2;

import com.shik2.earliest.Task;
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
public class ThetaInfluence {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果
        FileHelper taskFile = new FileHelper(DataProcess.TASKLISTPATH);
        List<InputVal> inputVals = taskFile.getObjFromFile();

        for (InputVal inputVal : inputVals) {
            System.out.println(inputVal);
        }

        for (int theta = 10; theta < 100; theta += 10) {
            int lamdaIDX = 19;  // 任务请求密度坐标
            logger.info("lamda=" + inputVals.get(lamdaIDX).getLamda() + " theta=" + theta);
            List<Task> tasks = inputVals.get(lamdaIDX).getTaskList();
            int urgencyTask = 0, normalTask = 0;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getUrgency() == 3) {
                    normalTask++;
                } else {
                    urgencyTask++;
                }
            }
            logger.info("紧急任务数：" + urgencyTask + " 普通任务数：" + normalTask);
            ResCompleteRate rcr = new ResCompleteRate(inputVals.get(lamdaIDX).getLamda());
            ResTotalDelay rtd = new ResTotalDelay(inputVals.get(lamdaIDX).getLamda());

            logger.info("==============================本文调度算法=====================================");
            StarterPaper.paperScheduleOnDifferentTheta(inputVals, lamdaIDX, (double) theta / 100.0, rcr, rtd);

           /* logger.info("==============================最近调度算法====================================");
            StarterClosest.closestSchdule(inputVals, m, rcr, rtd);

            logger.info("==============================完全遗传算法====================================");
            StarterGA.gaSchedule(inputVals, m, rcr, rtd);
            // EDF
            logger.info("==============================最先满足要求调度====================================");
            StarterEDF.firstFitSchdule(inputVals, m, rcr, rtd);

            // min-min
            logger.info("==============================min-min调度====================================");
            StarterMin.minSchedule(inputVals, m, rcr, rtd);

            //  DES
            logger.info("==============================DES调度====================================");
            StarterDES.desSchedule(inputVals, m, rcr, rtd);*/

            // 记录结果
            resCRList.add(rcr);
            resTDList.add(rtd);
        }

        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList) {
            logger.info(resCompleteRate.getPaperCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList) {
            logger.info(resTotalDelay.getPaperTDelay() + "");
        }
    }

}