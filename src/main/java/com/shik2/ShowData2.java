package com.shik2;

import com.shik2.earliest.Task;
import com.shik2.ga.MEC;
import com.shik2.starter.StarterPaper;
import com.shik2.utils.DataProcess;
import com.shik2.utils.FileHelper;
import com.shik2.utils.ListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shik2
 * @date 2021/01/09
 * <p>
 * Description: 调试，显示数据
 **/
public class ShowData2 {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        List<Task> tasks = TaskAndMECProduce.generateTasksWith2lambda(9,13, 30000);
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
