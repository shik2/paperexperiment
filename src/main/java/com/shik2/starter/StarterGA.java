package com.shik2.starter;

import com.shik2.ResCompleteRate;
import com.shik2.ResTotalDelay;
import com.shik2.Statistics;
import com.shik2.compare.GAAlgorithm;
import com.shik2.earliest.Task;
import com.shik2.ga.*;
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
public class StarterGA {

    static double currTime = 0;
    private static final Logger logger = LoggerFactory.getLogger(StarterGA.class);


    // 进度条粒度
    private static final int PROGRESS_SIZE = 50;
    private static final int BITE = 2;


    public static void main(String[] args) {
        FileHelper taskFile = new FileHelper(DataProcess.TASKLISTPATH);
        List<InputVal> inputVals = taskFile.getObjFromFile();

        for (InputVal inputVal : inputVals) {
            System.out.println(inputVal);
        }
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();

        int m = 23;
        logger.info("lamda: " + inputVals.get(m).getLamda());
        ResCompleteRate rs = new ResCompleteRate(inputVals.get(m).getLamda());
        ResTotalDelay rtd = new ResTotalDelay(inputVals.get(m).getLamda());
        gaSchedule(inputVals, m, mecList, rs, rtd);
    }


    /**
     * 完全遗传调度
     *
     * @param inputVals 任务总集
     * @param m         第m个任务列表
     * @param rs        结果保存对象
     */
    public static void gaSchedule(List<InputVal> inputVals, int m, List<MEC> oriMecList, ResCompleteRate rs, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间

        System.out.print("Progress:");
        int drawNum2 = 0, index2 = 0;
        String finish2 = getNChar(index2 / BITE, '█');
        String unFinish2 = getNChar(PROGRESS_SIZE - index2 / BITE, '─');
        String target2 = String.format("%3d%%[%s%s]", index2, finish2, unFinish2);
        System.out.print(target2);

        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);

        Statistics gaSts = new Statistics();
        int totalN = taskList.size();
        List<Task> normalTasks = new ArrayList<>();     // 遗传算法调度队列
        int popSize = 180;
        int acceptTaskNum = 0, urgencyTaskNum = 0, totalTaskNum = taskList.size();
        //调度开始
        while (taskList.size() > 0) {
            // 时间流逝
            if (!(taskList.get(0).getArriveTime() > currTime)) {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    // 移除完成的任务
                    for (MEC mec : mecList) {
                        mec.removeCompletedTasks(currTime, gaSts);
                    }

                    Task taskNow = taskList.get(0);
                    normalTasks.add(taskNow);

                    if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                        gaSts.urgencyTask++;
                    }

                    if (normalTasks.size() == 10) {   //遗传算法触发条件
                        int[] assginStrategy = getGaStrategy(mecList, normalTasks, popSize);
                        // 分配任务
                        for (int i = 0; i < assginStrategy.length; i++) {
                            MEC chooseMec = mecList.get(assginStrategy[i]);
                            Task assginTask = normalTasks.get(i);
                            if (GAAlgorithm.acceptTask(chooseMec, assginTask, currTime)) {
                                if (assginTask.getUrgency() == 1 || assginTask.getUrgency() == 2) {
                                    gaSts.finishUrgencyTask++;
                                }
                            }
                        }
                        normalTasks.clear();
                    }
                    taskList.remove(0);
                    drawNum2++;
                }
            }

            finish2 = getNChar(index2 / BITE, '█');
            unFinish2 = getNChar(PROGRESS_SIZE - index2 / BITE, '─');
            target2 = String.format("%3d%%├%s%s┤", index2, finish2, unFinish2);
            System.out.print(getNChar(PROGRESS_SIZE + 6, '\b'));
            System.out.print(target2);
            index2 = (int) (1 + 100.0 * drawNum2 / totalN);

            timeFlies();    // 时间流逝
            continue;

        }

        while (!isAllTaskFinish(mecList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : mecList) {
                mec.removeCompletedTasks(currTime, gaSts);
            }
            timeFlies();
        }

        double cRate = DataProcess.format(gaSts.finishUrgencyTask * 100 / (double) gaSts.urgencyTask, 2);
        double tDelay = DataProcess.format(gaSts.totalNormalDelay / gaSts.normalTask, 2);

        logger.info("");
        logger.info("完成紧急任务数：" + gaSts.finishUrgencyTask);
        logger.info("总共紧急任务数：" + gaSts.urgencyTask);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalTaskNum);
        logger.info("移除普通任务数：" + gaSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rs.setGaCRate(cRate);
        rtd.setGaTDelay(tDelay);
    }




    public static void gaThetaSchedule(List<InputVal> inputVals, int m, int gaTheta,List<MEC> oriMecList, ResCompleteRate rs, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间

        System.out.print("Progress:");
        int drawNum2 = 0, index2 = 0;
        String finish2 = getNChar(index2 / BITE, '█');
        String unFinish2 = getNChar(PROGRESS_SIZE - index2 / BITE, '─');
        String target2 = String.format("%3d%%[%s%s]", index2, finish2, unFinish2);
        System.out.print(target2);

        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);

        Statistics gaSts = new Statistics();
        int totalN = taskList.size();
        List<Task> normalTasks = new ArrayList<>();     // 遗传算法调度队列
        int popSize = 180;
        int acceptTaskNum = 0, urgencyTaskNum = 0, totalTaskNum = taskList.size();
        //调度开始
        while (taskList.size() > 0) {
            // 时间流逝
            if (!(taskList.get(0).getArriveTime() > currTime)) {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    // 移除完成的任务
                    for (MEC mec : mecList) {
                        mec.removeCompletedTasks(currTime, gaSts);
                    }

                    Task taskNow = taskList.get(0);
                    normalTasks.add(taskNow);

                    if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                        gaSts.urgencyTask++;
                    }

                    if (normalTasks.size() == gaTheta) {   //遗传算法触发条件
                        int[] assginStrategy = getGaStrategy(mecList, normalTasks, popSize);
                        // 分配任务
                        for (int i = 0; i < assginStrategy.length; i++) {
                            MEC chooseMec = mecList.get(assginStrategy[i]);
                            Task assginTask = normalTasks.get(i);
                            if (GAAlgorithm.acceptTask(chooseMec, assginTask, currTime)) {
                                if (assginTask.getUrgency() == 1 || assginTask.getUrgency() == 2) {
                                    gaSts.finishUrgencyTask++;
                                }
                            }
                        }
                        normalTasks.clear();
                    }
                    taskList.remove(0);
                    drawNum2++;
                }
            }


            finish2 = getNChar(index2 / BITE, '█');
            unFinish2 = getNChar(PROGRESS_SIZE - index2 / BITE, '─');
            target2 = String.format("%3d%%├%s%s┤", index2, finish2, unFinish2);
            System.out.print(getNChar(PROGRESS_SIZE + 6, '\b'));
            System.out.print(target2);
            index2 = (int) (1 + 100.0 * drawNum2 / totalN);

            timeFlies();    // 时间流逝
            continue;

        }

        while (!isAllTaskFinish(mecList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : mecList) {
                mec.removeCompletedTasks(currTime, gaSts);
            }
            timeFlies();
        }

        if (normalTasks.size() > 0) {   //遗传算法触发条件
            int[] assginStrategy = getGaStrategy(mecList, normalTasks, popSize);
            // 分配任务
            for (int i = 0; i < assginStrategy.length; i++) {
                MEC chooseMec = mecList.get(assginStrategy[i]);
                chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
            }
            normalTasks.clear();
        }


        double cRate = DataProcess.format(gaSts.finishUrgencyTask * 100 / (double) gaSts.urgencyTask, 2);
        double tDelay = DataProcess.format(gaSts.totalNormalDelay / gaSts.normalTask, 2);

        logger.info("");
        logger.info("完成紧急任务数：" + gaSts.finishUrgencyTask);
        logger.info("总共紧急任务数：" + gaSts.urgencyTask);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalTaskNum);
        logger.info("移除普通任务数：" + gaSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rs.setGaCRate(cRate);
        rtd.setGaTDelay(tDelay);
    }


    /**
     * 测试GA阈值性能，没有卸载失败的情况下
     * @param
     * @param
     * @param gaTheta
     * @param oriMecList
     * @param rs
     * @param rtd
     */
    public static void ga(List<Task> tasks, int gaTheta,List<MEC> oriMecList, ResCompleteRate rs, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间

        System.out.print("Progress:");
        int drawNum2 = 0, index2 = 0;
        String finish2 = getNChar(index2 / BITE, '█');
        String unFinish2 = getNChar(PROGRESS_SIZE - index2 / BITE, '─');
        String target2 = String.format("%3d%%[%s%s]", index2, finish2, unFinish2);
        System.out.print(target2);

        List<Task> taskList = DeepCopy.deepCopy(tasks);
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);

        Statistics gaSts = new Statistics();
        int totalN = taskList.size();
        List<Task> normalTasks = new ArrayList<>();     // 遗传算法调度队列
        int popSize = 180;
        int acceptTaskNum = 0, urgencyTaskNum = 0, totalTaskNum = taskList.size();
        //调度开始
        while (taskList.size() > 0) {
            // 时间流逝
            if (!(taskList.get(0).getArriveTime() > currTime)) {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    // 移除完成的任务
                    for (MEC mec : mecList) {
                        mec.removeCompletedTasks(currTime, gaSts);
                    }

                    Task taskNow = taskList.get(0);
                    normalTasks.add(taskNow);

                    if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                        gaSts.urgencyTask++;
                    }

                    if (normalTasks.size() == gaTheta) {   //遗传算法触发条件
                        int[] assginStrategy = getGaStrategy(mecList, normalTasks, popSize);
                        // 分配任务
                        for (int i = 0; i < assginStrategy.length; i++) {
                            MEC chooseMec = mecList.get(assginStrategy[i]);
                            Task assginTask = normalTasks.get(i);
                            if (GAAlgorithm.acceptTask2(chooseMec, assginTask, currTime)) {
                                if (assginTask.getUrgency() == 1 || assginTask.getUrgency() == 2) {
                                    gaSts.finishUrgencyTask++;
                                }
                            }
                        }
                        normalTasks.clear();
                    }
                    taskList.remove(0);
                    drawNum2++;
                }
            }


            finish2 = getNChar(index2 / BITE, '█');
            unFinish2 = getNChar(PROGRESS_SIZE - index2 / BITE, '─');
            target2 = String.format("%3d%%├%s%s┤", index2, finish2, unFinish2);
            System.out.print(getNChar(PROGRESS_SIZE + 6, '\b'));
            System.out.print(target2);
            index2 = (int) (1 + 100.0 * drawNum2 / totalN);

            timeFlies();    // 时间流逝
            continue;

        }

        while (!isAllTaskFinish(mecList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : mecList) {
                mec.removeCompletedTasks(currTime, gaSts);
            }
            timeFlies();
        }

        if (normalTasks.size() > 0) {   //遗传算法触发条件
            int[] assginStrategy = getGaStrategy(mecList, normalTasks, popSize);
            // 分配任务
            for (int i = 0; i < assginStrategy.length; i++) {
                MEC chooseMec = mecList.get(assginStrategy[i]);
                chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
            }
            normalTasks.clear();
        }


        double cRate = DataProcess.format(gaSts.finishUrgencyTask * 100 / (double) gaSts.urgencyTask, 2);
        double tDelay = DataProcess.format(gaSts.totalNormalDelay / gaSts.normalTask, 2);

        logger.info("");
        logger.info("完成紧急任务数：" + gaSts.finishUrgencyTask);
        logger.info("总共紧急任务数：" + gaSts.urgencyTask);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalTaskNum);
        logger.info("移除普通任务数：" + gaSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rs.setGaCRate(cRate);
        rtd.setGaTDelay(tDelay);
    }




    public static void timeFlies() {
        currTime = DataProcess.format(currTime + 0.01, 2);
    }

    /**
     * 绘制进度条
     *
     * @param num
     * @param ch
     * @return
     */
    private static String getNChar(int num, char ch) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    public static int[] getGaStrategy(List<MEC> afterMECList, List<Task> normalTasks, int popSize) {
        // 设置遗传算法调度器的参数
        ScheduleData.setMecList(afterMECList);
        ScheduleData.setTaskList(normalTasks);
        Individual.setDefaultGeneLength(normalTasks.size());
        Population mergePop = new Population(popSize);
        // 初始化3个种群
        Population myPop1 = new Population(popSize / 3, true, currTime, 0.7, 0.08);
        Population myPop2 = new Population(popSize / 3, true, currTime, 0.6, 0.05);
        Population myPop3 = new Population(popSize / 3, true, currTime, 0.65, 0.06);
        // 迭代，进行进化操作。 直到找到期望的基因序列
        int generationCount = 0;
        double maxFitness = Math.max(Math.max(myPop1.getFittest().getFitness(), myPop2.getFittest().getFitness()), myPop3.getFittest().getFitness());
        while (generationCount < 50) {
            generationCount++;
            myPop1 = Algorithm.evolvePopulation(myPop1, currTime);
            myPop2 = Algorithm.evolvePopulation(myPop2, currTime);
            myPop3 = Algorithm.evolvePopulation(myPop3, currTime);
//                                    maxFitness = Math.max(Math.max(myPop1.getFittest().getFitness(), myPop2.getFittest().getFitness()), myPop3.getFittest().getFitness());
//                                    logger.info("第" + generationCount + "次进化，种群最佳适应度为： " + maxFitness);
            // 每经过5次迭代进行一次迁移
            if (generationCount % 5 == 0) {
                Algorithm.migratePopulation(myPop1, myPop2);
                Algorithm.migratePopulation(myPop2, myPop3);
                Algorithm.migratePopulation(myPop3, myPop1);
            }
        }
        for (int i = 0; i < mergePop.size(); i++) {
            if (i < popSize / 3) {
                mergePop.saveIndividual(i, myPop1.getIndividuals()[i]);
            } else if (i < 2 * popSize / 3) {
                mergePop.saveIndividual(i, myPop2.getIndividuals()[i - popSize / 3]);
            } else {
                mergePop.saveIndividual(i, myPop3.getIndividuals()[i - 2 * popSize / 3]);
            }
        }
        return mergePop.getFittest().getGenes();

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
