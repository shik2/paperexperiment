package com.shik2;

import com.shik2.earliest.Task;
import com.shik2.ga.MEC;
import com.shik2.input.InputVal;
import com.shik2.starter.StarterPaper;
import com.shik2.utils.DataProcess;
import com.shik2.utils.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author shik2
 * @date 2021/01/09
 * <p>
 * Description: 调试，显示数据
 **/
public class ShowData {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

//        List<Task> tasks = TaskAndMECProduce.generateTasksWith2lambda(10,15, 90000);
        List<Task> tasks = TaskAndMECProduce.generateTasks(13, 10000);
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();

        ResCompleteRate rcr = new ResCompleteRate(1);
        ResTotalDelay rtd = new ResTotalDelay(1);

        logger.info("==============================本文调度算法=====================================");
        StarterPaper.paperScheduleOndifTheta(tasks, 0.8, rcr, rtd);
    }


}
