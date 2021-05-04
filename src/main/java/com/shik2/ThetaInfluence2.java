package com.shik2;

import com.shik2.earliest.Task;
import com.shik2.input.InputVal;
import com.shik2.starter.StarterDES;
import com.shik2.starter.StarterPaper;
import com.shik2.utils.DataProcess;
import com.shik2.utils.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.shik2.TaskAndMECProduce.generateTasks;

/**
 * @author shik2
 * @date 2021/01/07
 * <p>
 * Description:
 **/
public class ThetaInfluence2 {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果

        List<InputVal> inputVals = new ArrayList<>();
        for (int lamda = 100; lamda <= 100; lamda += 2) {
            List<Task> tasks = generateTasks(lamda, 5000);
            inputVals.add(new InputVal(lamda, tasks));
        }

        for (InputVal inputVal : inputVals) {
            System.out.println(inputVal);
        }

        for (int theta = 10; theta < 100; theta += 10) {
            int lamda = 15;
            logger.info("lamda=" + inputVals.get(lamda).getLamda() + " theta=" + theta);
            List<Task> tasks = inputVals.get(lamda).getTaskList();
            int urgencyTask = 0, normalTask = 0;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getUrgency() == 3) {
                    normalTask++;
                } else {
                    urgencyTask++;
                }
            }
            logger.info("紧急任务数：" + urgencyTask + " 普通任务数：" + normalTask);
            ResCompleteRate rcr = new ResCompleteRate(inputVals.get(lamda).getLamda());
            ResTotalDelay rtd = new ResTotalDelay(inputVals.get(lamda).getLamda());

            logger.info("==============================本文调度算法=====================================");
            StarterPaper.paperScheduleOnDifferentTheta(inputVals, lamda, (double) theta / 100, rcr, rtd);

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


        List<ResCompleteRate> resCRList2 = new ArrayList<>();  // 存放各lamda完成率结果
        List<ResTotalDelay> resTDList2 = new ArrayList<>();  // 存放各lamda普通任务时延结果

        for (int m = 0; m < inputVals.size(); m++) {
            logger.info("lamda: " + inputVals.get(m).getLamda());
            ResCompleteRate rs = new ResCompleteRate(inputVals.get(m).getLamda());
            ResTotalDelay rtd = new ResTotalDelay(inputVals.get(m).getLamda());
            StarterDES.desSchedule(inputVals, m, rs, rtd);
            // 记录结果
            resCRList2.add(rs);
            resTDList2.add(rtd);

        }

        logger.info("-------------------------------------------------");
        for (ResCompleteRate resCompleteRate : resCRList2) {
            logger.info(resCompleteRate.getDesCRate() + "");
        }
        logger.info("-------------------------------------------------");
        for (ResTotalDelay resTotalDelay : resTDList2) {
            logger.info(resTotalDelay.getDesTDelay() + "");
        }
    }

}