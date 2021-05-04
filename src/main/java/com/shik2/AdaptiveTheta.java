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
 * @date 2021/03/12
 * <p>
 * Description:
 **/
public class AdaptiveTheta {

    private static final Logger logger = LoggerFactory.getLogger(com.shik2.Main.class);

    public static void main(String[] args) {
        List<Task> tasks = TaskAndMECProduce.generateTasksWith2lambda(9,12, 40000);
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();



        ResCompleteRate rcr = new ResCompleteRate(1);
        ResTotalDelay rtd = new ResTotalDelay(1);

        logger.info("==============================本文调度算法(带自适应)=====================================");
        StarterPaper.paperScheduleWithAdaptive(tasks, mecList, rcr, rtd);

        logger.info("==============================本文调度算法(不自适应)=====================================");
        StarterPaper.paperSchedule2(tasks, mecList, rcr, rtd);
    }

}

