package com.shik2.starter;

import com.shik2.ResCompleteRate;
import com.shik2.ResTotalDelay;
import com.shik2.Statistics;
import com.shik2.earliest.DESAlgorithm;
import com.shik2.earliest.RegroupParam;
import com.shik2.earliest.Task;
import com.shik2.ga.*;
import com.shik2.input.InputVal;
import com.shik2.utils.DataProcess;
import com.shik2.utils.DeepCopy;
import com.shik2.utils.FileHelper;
import com.shik2.utils.ListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.shik2.TaskAndMECProduce.generateTasks;

/**
 * @author shik2
 * @date 2020/12/30
 * <p>
 * Description:
 **/
public class StarterPaper {

    static double currTime = 0;
    static double interval = 0;  // 两次遗传算法间隔
    private static Logger logger = LoggerFactory.getLogger(StarterPaper.class);

    // 进度条粒度
    private static final int PROGRESS_SIZE = 50;
    private static int BITE = 2;

    public static void main(String[] args) {

        List<List<ResCompleteRate>> lists1 = new ArrayList<>();
        List<List<ResTotalDelay>> lists2 = new ArrayList<>();

        for (int i = 0; i < 20; i++) {


//            FileHelper taskFile = new FileHelper(DataProcess.TASKLISTPATH);
//            List<InputVal> inputVals = taskFile.getObjFromFile();

            List<InputVal> inputVals = new ArrayList<>();
            for (int lamda = 500; lamda <= 500; lamda += 1) {
                List<Task> tasks = generateTasks(lamda, 1000);
                inputVals.add(new InputVal(lamda, tasks));
            }

            for (InputVal inputVal : inputVals) {
                System.out.println(inputVal);
            }

            FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
            List<MEC> mecList = mecFile.getObjFromFile();


            List<ResCompleteRate> resCRList = new ArrayList<>();  // 存放各lamda完成率结果
            List<ResTotalDelay> resTDList = new ArrayList<>();  // 存放各lamda普通任务时延结果

            for (int m = 0; m < inputVals.size(); m++) {
                logger.info("lamda: " + inputVals.get(m).getLamda());
                ResCompleteRate rs = new ResCompleteRate(inputVals.get(m).getLamda());
                ResTotalDelay rtd = new ResTotalDelay(inputVals.get(m).getLamda());
                paperSchedule(inputVals, m, mecList, rs, rtd);
                // 记录结果
                resCRList.add(rs);
                resTDList.add(rtd);
            }
            lists1.add(resCRList);
            lists2.add(resTDList);
        }
        logger.info("------------");
        for (int i = 0; i < lists1.get(0).size(); i++) {
            double cSum = 0;
            for (int j = 0; j < lists1.size(); j++) {
                cSum += lists1.get(j).get(i).getPaperCRate();
            }
            logger.info(cSum / 20 + "");
        }
        logger.info("------------");
        for (int i = 0; i < lists1.get(0).size(); i++) {
            double dSum = 0;
            for (int j = 0; j < lists1.size(); j++) {
                dSum += lists2.get(j).get(i).getPaperTDelay();
            }
            logger.info(dSum / 20 + "");
        }


    }


    /**
     * 本文调度算法
     *
     * @param inputVals 任务总集
     * @param m         第m个任务列表
     * @param rcr       结果保存对象
     */
    public static void paperSchedule(List<InputVal> inputVals, int m, List<MEC> oriMecList, ResCompleteRate rcr, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        // 获取任务集和MEC集
        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);

        System.out.print("Progress:");
        Statistics paperSts = new Statistics(); // 统计信息对象
        int directNum = 0, rePlaceNum = 0, failNum = 0, preemptNum = 0;
        double calMount = 0; // 紧急任务的计算量
        int task45_5 = 0, task4_45 = 0;

        int totalN = taskList.size();
        int drawNum = 0, index = 0;
        String finish = getNChar(index / BITE, '█');
        String unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');
        String target = String.format("%3d%%[%s%s]", index, finish, unFinish);
        System.out.print(target);

        // 按照计算能力划分MEC
        Collections.sort(mecList, (t1, t2) -> t1.getC() - t2.getC() < 0 ? 1 : (t1.getC() - t2.getC() == 0) ? 0 : -1);
        int theta = (int) (0.8 * mecList.size());
        List<MEC> beforeMECList = new ArrayList<>(mecList.subList(0, theta));   // subList为视图而非实体对象，无法深拷贝
        List<MEC> afterMECList = new ArrayList<>(mecList.subList(theta, mecList.size()));


        //调度开始
        List<Task> normalTasks = new ArrayList<>();     // 普通任务队列，采用遗传算法调度
        int popSize = 180;  // 合并种群大小
        while (taskList.size() > 0) {
            if (taskList.get(0).getArriveTime() > currTime) {
                timeFlies();    // 时间流逝
                continue;
            } else {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    // 移除所有MEC中已经完成的任务
                    for (MEC mec : beforeMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }
                    for (MEC mec : afterMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }

                    Task taskNow = taskList.get(0);

                    if (taskNow.getUrgency() != 3) {  // 紧急任务单任务分配
                        paperSts.urgencyTask++;
                        // 选取最优的MEC
                        MEC bestMEC = DESAlgorithm.getBestMEC(beforeMECList, taskNow, currTime);
                        if (bestMEC != null) {
                            bestMEC.acceptAndUpdateTime(taskNow, currTime);
                            directNum++;
                            calMount += taskNow.getL();
                            if (taskNow.getL() >= 4.5) {
                                task45_5++;
                            } else if (taskNow.getL() >= 4) {
                                task4_45++;
                            }
                        } else {
                            // 获取迁移方案
                            RegroupParam replaceRes = DESAlgorithm.regroupTask(beforeMECList, taskNow, currTime);
                            if (replaceRes != null) {
                                MEC selectMEC = null;
                                for (MEC mec : beforeMECList) {
                                    if (mec.getId() == replaceRes.getMec().getId()) {
                                        selectMEC = mec;
                                    }
                                }
                                // 先将选中的MEC任务按迁移策略迁移
                                for (Map.Entry entry : replaceRes.getTransfer().entrySet()) {
                                    Task task = (Task) entry.getKey();
                                    selectMEC.remove(task);
                                    selectMEC.updateTimeAfterRemove();

                                    MEC transferMEC = null;
                                    for (MEC mec : beforeMECList) {
                                        if (mec.getId() == replaceRes.getTransfer().get(task).getId()) {
                                            transferMEC = mec;
                                        }
                                    }
                                    transferMEC.acceptAndUpdateTime(task, currTime);
                                }

                                // 任务执行
                                selectMEC.acceptAndUpdateTime(taskNow, currTime);
                                rePlaceNum++;
                                calMount += taskNow.getL();
                                if (taskNow.getL() >= 4.5) {
                                    task45_5++;
                                } else if (taskNow.getL() >= 4) {
                                    task4_45++;
                                }
                            } else {
                                MEC tmpUseMEC = DESAlgorithm.getBestMEC(afterMECList, taskNow, currTime);
                                if (tmpUseMEC != null) {
                                    tmpUseMEC.acceptAndUpdateTime(taskNow, currTime);
                                    preemptNum++;
                                    calMount += taskNow.getL();
                                    if (taskNow.getL() >= 4.5) {
                                        task45_5++;
                                    } else if (taskNow.getL() >= 4) {
                                        task4_45++;
                                    }
                                } else {
                                    failNum++;
                                }
                            }
                        }
                    } else {  // 非紧急任务加入到遗传算法的调度队列
                        normalTasks.add(taskNow);
                        if (normalTasks.size() == 10) {   //遗传算法触发条件
                            {
                                int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
                                // 分配任务
                                for (int i = 0; i < assginStrategy.length; i++) {
                                    MEC chooseMec = afterMECList.get(assginStrategy[i]);
                                    chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
                                }

                                normalTasks.clear();
                                interval = 0;
                            }
                        }
                    }
                    taskList.remove(0);
                    drawNum++;
                }

                finish = getNChar(index / BITE, '█');
                unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');

                target = String.format("%3d%%├%s%s┤", index, finish, unFinish);
                System.out.print(getNChar(PROGRESS_SIZE + 6, '\b'));
                System.out.print(target);
                index = (int) (1 + 100.0 * drawNum / totalN);


                timeFlies();    // 时间流逝
                continue;
            }

        }

        if (normalTasks.size() > 0) {   //遗传算法触发条件
            int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
            // 分配任务
            for (int i = 0; i < assginStrategy.length; i++) {
                MEC chooseMec = afterMECList.get(assginStrategy[i]);
                chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
            }
            normalTasks.clear();
        }


        while (!isAllTaskFinish(beforeMECList) || !isAllTaskFinish(afterMECList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : beforeMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            for (MEC mec : afterMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            timeFlies();
        }


        double cRate = DataProcess.format((paperSts.urgencyTask - failNum) * 100 / (double) paperSts.urgencyTask, 2);   // 紧急任务成功率
        double tDelay = DataProcess.format(paperSts.totalNormalDelay / paperSts.normalTask, 2); // 普通任务平均时延

        logger.info("");
        logger.info("直接执行：" + directNum);
        logger.info("重组执行：" + rePlaceNum);
        logger.info("临时抢占：" + preemptNum);
        logger.info("卸载失败：" + failNum);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalN);
        logger.info("移除普通任务数：" + paperSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rcr.setPaperCRate(cRate);
        rtd.setPaperTDelay(tDelay);
    }


    public static void paperSchedule2(List<Task> oriTasks, List<MEC> oriMecList, ResCompleteRate rcr, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        // 获取任务集和MEC集
        List<Task> taskList = DeepCopy.deepCopy(oriTasks);
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);

        System.out.print("Progress:");
        Statistics paperSts = new Statistics(); // 统计信息对象
        int directNum = 0, rePlaceNum = 0, failNum = 0, preemptNum = 0;
        double calMount = 0; // 紧急任务的计算量
        int task45_5 = 0, task4_45 = 0;

        int totalN = taskList.size();
        int drawNum = 0, index = 0;
        String finish = getNChar(index / BITE, '█');
        String unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');
        String target = String.format("%3d%%[%s%s]", index, finish, unFinish);
        System.out.print(target);

        // 按照计算能力划分MEC
        Collections.sort(mecList, (t1, t2) -> t1.getC() - t2.getC() < 0 ? 1 : (t1.getC() - t2.getC() == 0) ? 0 : -1);
        int theta = (int) (0.5 * mecList.size());
        List<MEC> beforeMECList = new ArrayList<>(mecList.subList(0, theta));   // subList为视图而非实体对象，无法深拷贝
        List<MEC> afterMECList = new ArrayList<>(mecList.subList(theta, mecList.size()));


        //结果统计参数
        double resPoint = 0;
        int res_etaskNum = 0;     //周期内紧急任务总数
        int res_failEtaskNum = 0;  //周期内紧急任务完成数
        int res_count = 0;
        double ecr = 0;
        double res_theta = 0;
        double res_lambda = 0;
        double res_ntaskTime = 0;
        Statistics last_paperSts = new Statistics(); // 统计信息对象

        List<Double> lamda_list = new ArrayList<>();
        List<Double> theta_list = new ArrayList<>();
        List<Double> ecr_list = new ArrayList<>();
        List<Double> nat_list = new ArrayList<>();


        //调度开始
        List<Task> normalTasks = new ArrayList<>();     // 普通任务队列，采用遗传算法调度
        int popSize = 180;  // 合并种群大小
        while (taskList.size() > 0) {
            if (taskList.get(0).getArriveTime() > currTime) {
                timeFlies();    // 时间流逝
                continue;
            } else {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    res_count++;
                    // 移除所有MEC中已经完成的任务
                    for (MEC mec : beforeMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }
                    for (MEC mec : afterMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }

                    Task taskNow = taskList.get(0);

                    if (taskNow.getUrgency() != 3) {  // 紧急任务单任务分配
                        paperSts.urgencyTask++;
                        res_etaskNum++;
                        // 选取最优的MEC
                        MEC bestMEC = DESAlgorithm.getBestMEC(beforeMECList, taskNow, currTime);
                        if (bestMEC != null) {
                            bestMEC.acceptAndUpdateTime(taskNow, currTime);
                            directNum++;
                            calMount += taskNow.getL();
                            if (taskNow.getL() >= 4.5) {
                                task45_5++;
                            } else if (taskNow.getL() >= 4) {
                                task4_45++;
                            }
                        } else {
                            // 获取迁移方案
                            RegroupParam replaceRes = DESAlgorithm.regroupTask(beforeMECList, taskNow, currTime);
                            if (replaceRes != null) {
                                MEC selectMEC = null;
                                for (MEC mec : beforeMECList) {
                                    if (mec.getId() == replaceRes.getMec().getId()) {
                                        selectMEC = mec;
                                    }
                                }
                                // 先将选中的MEC任务按迁移策略迁移
                                for (Map.Entry entry : replaceRes.getTransfer().entrySet()) {
                                    Task task = (Task) entry.getKey();
                                    selectMEC.remove(task);
                                    selectMEC.updateTimeAfterRemove();

                                    MEC transferMEC = null;
                                    for (MEC mec : beforeMECList) {
                                        if (mec.getId() == replaceRes.getTransfer().get(task).getId()) {
                                            transferMEC = mec;
                                        }
                                    }
                                    transferMEC.acceptAndUpdateTime(task, currTime);
                                }

                                // 任务执行
                                selectMEC.acceptAndUpdateTime(taskNow, currTime);
                                rePlaceNum++;
                                calMount += taskNow.getL();
                                if (taskNow.getL() >= 4.5) {
                                    task45_5++;
                                } else if (taskNow.getL() >= 4) {
                                    task4_45++;
                                }
                            } else {
                                MEC tmpUseMEC = DESAlgorithm.getBestMEC(afterMECList, taskNow, currTime);
                                if (tmpUseMEC != null) {
                                    tmpUseMEC.acceptAndUpdateTime(taskNow, currTime);
                                    preemptNum++;
                                    calMount += taskNow.getL();
                                    if (taskNow.getL() >= 4.5) {
                                        task45_5++;
                                    } else if (taskNow.getL() >= 4) {
                                        task4_45++;
                                    }
                                } else {
                                    failNum++;
                                    res_failEtaskNum++;
                                }
                            }
                        }
                    } else {  // 非紧急任务加入到遗传算法的调度队列
                        normalTasks.add(taskNow);
                        if (normalTasks.size() == 10) {   //遗传算法触发条件
                            {
                                int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
                                // 分配任务
                                for (int i = 0; i < assginStrategy.length; i++) {
                                    MEC chooseMec = afterMECList.get(assginStrategy[i]);
                                    chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
                                }

                                normalTasks.clear();
                                interval = 0;
                            }
                        }
                    }
                    taskList.remove(0);
                    drawNum++;
                }


                if (currTime - resPoint > 150) {
                    res_lambda = res_count / (currTime - resPoint);
                    ecr = DataProcess.format((double) (res_etaskNum - res_failEtaskNum) * 100 / res_etaskNum, 2);
                    res_theta = beforeMECList.size() / 30.0;
                    res_ntaskTime = (paperSts.totalNormalDelay) / (paperSts.normalTask);
                    lamda_list.add(res_lambda);
                    theta_list.add(res_theta);
                    ecr_list.add(ecr);
                    nat_list.add(res_ntaskTime);
                    System.out.println(res_lambda + " " + res_theta + " " + ecr + " " + res_ntaskTime);


                    res_etaskNum = 0;
                    res_failEtaskNum = 0;
                    res_count = 0;
                    resPoint = currTime;
                    last_paperSts.totalNormalDelay = paperSts.totalNormalDelay;
                    last_paperSts.normalTask = paperSts.normalTask;
                }


                timeFlies();    // 时间流逝
                continue;
            }

        }

        if (normalTasks.size() > 0) {   //遗传算法触发条件
            int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
            // 分配任务
            for (int i = 0; i < assginStrategy.length; i++) {
                MEC chooseMec = afterMECList.get(assginStrategy[i]);
                chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
            }
            normalTasks.clear();
        }


        while (!isAllTaskFinish(beforeMECList) || !isAllTaskFinish(afterMECList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : beforeMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            for (MEC mec : afterMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            timeFlies();
        }


        double cRate = DataProcess.format((paperSts.urgencyTask - failNum) * 100 / (double) paperSts.urgencyTask, 2);   // 紧急任务成功率
        double tDelay = DataProcess.format(paperSts.totalNormalDelay / paperSts.normalTask, 2); // 普通任务平均时延

        logger.info("");
        logger.info("直接执行：" + directNum);
        logger.info("重组执行：" + rePlaceNum);
        logger.info("临时抢占：" + preemptNum);
        logger.info("卸载失败：" + failNum);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalN);
        logger.info("移除普通任务数：" + paperSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rcr.setPaperCRate(cRate);
        rtd.setPaperTDelay(tDelay);


        logger.info("================================");
        for (Double aDouble : lamda_list) {
            logger.info("lambda=" + aDouble);
        }
        for (Double aDouble : theta_list) {
            logger.info("theta=" + aDouble);
        }
        for (Double aDouble : ecr_list) {
            logger.info("ecr=" + aDouble);
        }
        for (Double aDouble : nat_list) {
            logger.info("nat=" + aDouble);
        }
    }

    /**
     * 本文卸载策略（带有MEC资源自适应机制）
     */
    public static void paperScheduleWithAdaptive(List<Task> tasks, List<MEC> oriMecList, ResCompleteRate rcr, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        // 获取任务集和MEC集
        List<Task> taskList = DeepCopy.deepCopy(tasks);
        List<MEC> mecList = DeepCopy.deepCopy(oriMecList);

        Statistics paperSts = new Statistics(); // 统计信息对象
        int directNum = 0, rePlaceNum = 0, failNum = 0, preemptNum = 0;

        int totalN = taskList.size();
        int drawNum = 0, index = 0;

        // 按照计算能力初始化ECS划分
        Collections.sort(mecList, (t1, t2) -> t1.getC() - t2.getC() < 0 ? 1 : (t1.getC() - t2.getC() == 0) ? 0 : -1);
        int theta = (int) (0.5 * mecList.size());
        List<MEC> beforeMECList = new ArrayList<>(mecList.subList(0, theta));   // subList为视图而非实体对象，无法深拷贝
        List<MEC> afterMECList = new ArrayList<>(mecList.subList(theta, mecList.size()));

        List<Task> normalTasks = new ArrayList<>();     // 普通任务队列，采用遗传算法调度
        int popSize = 150;  // 合并种群大小

        //自适应机制参数
        int interval_ugtaskNum = 0;     //周期内紧急任务总数
        int interval_failUgTaskNum = 0;  //周期内紧急任务完成数
        double interval = 0;
        double lastPoint = 0;
        double point = 0;  //上一个计时点
        double lambda_pre = 0;
        double lambda_now = 0;
        int count = 0;
        int a = 4;
        int isInAdaptflag = 0;
        int left = 0; // 左移标志


        //结果统计参数
        double resPoint = 0;
        int res_etaskNum = 0;     //周期内紧急任务总数
        int res_failEtaskNum = 0;  //周期内紧急任务完成数
        int res_count = 0;
        double ecr = 0;
        double res_theta = 0;
        double res_lambda = 0;
        double res_ntaskTime = 0;
        Statistics last_paperSts = new Statistics(); // 统计信息对象

        List<Double> lamda_list = new ArrayList<>();
        List<Double> theta_list = new ArrayList<>();
        List<Double> ecr_list = new ArrayList<>();
        List<Double> nat_list = new ArrayList<>();


        //调度开始
        while (taskList.size() > 0) {

            if (taskList.get(0).getArriveTime() > currTime) {
                timeFlies();    // 时间流逝
                continue;
            } else {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {

                    count++;
                    res_count++;
                    // 移除所有MEC中已经完成的任务
                    for (MEC mec : beforeMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }
                    for (MEC mec : afterMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }

                    Task taskNow = taskList.get(0);
//                    logger.info("当前任务：" + taskNow + " 当前时间：" + currTime);
                    if (taskNow.getUrgency() != 3) {  // 紧急任务单任务分配
                        paperSts.urgencyTask++;
                        if (isInAdaptflag == 1) {
                            interval_ugtaskNum++;
                        }
                        res_etaskNum++;
                        // 选取最优的MEC
                        MEC bestMEC = DESAlgorithm.getBestMEC(beforeMECList, taskNow, currTime);
                        if (bestMEC != null) {
                            bestMEC.acceptAndUpdateTime(taskNow, currTime);
                            directNum++;
                        } else {
                            // 获取迁移方案
                            RegroupParam replaceRes = DESAlgorithm.regroupTask(beforeMECList, taskNow, currTime);
                            if (replaceRes != null) {
                                MEC selectMEC = null;
                                for (MEC mec : beforeMECList) {
                                    if (mec.getId() == replaceRes.getMec().getId()) {
                                        selectMEC = mec;
                                    }
                                }
                                // 先将选中的MEC任务按迁移策略迁移
                                for (Map.Entry entry : replaceRes.getTransfer().entrySet()) {
                                    Task task = (Task) entry.getKey();
                                    selectMEC.remove(task);
                                    selectMEC.updateTimeAfterRemove();

                                    MEC transferMEC = null;
                                    for (MEC mec : beforeMECList) {
                                        if (mec.getId() == replaceRes.getTransfer().get(task).getId()) {
                                            transferMEC = mec;
                                        }
                                    }
                                    transferMEC.acceptAndUpdateTime(task, currTime);
                                }

                                // 任务执行
                                selectMEC.acceptAndUpdateTime(taskNow, currTime);
                                rePlaceNum++;
                            } else {
                                MEC tmpUseMEC = DESAlgorithm.getBestMEC(afterMECList, taskNow, currTime);
                                if (tmpUseMEC != null) {
                                    tmpUseMEC.acceptAndUpdateTime(taskNow, currTime);
                                    preemptNum++;
                                } else {
                                    if (isInAdaptflag == 1) {
                                        interval_failUgTaskNum++;
                                    }
                                    failNum++;
                                    res_failEtaskNum++;
                                }
                            }
                        }
                    } else {  // 非紧急任务加入到遗传算法的调度队列
                        normalTasks.add(taskNow);
                        if (normalTasks.size() == 10) {   //遗传算法触发条件
                            {
                                int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
                                // 分配任务
                                for (int i = 0; i < assginStrategy.length; i++) {
                                    MEC chooseMec = afterMECList.get(assginStrategy[i]);
                                    chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
                                }
                                normalTasks.clear();
                            }
                        }
                    }
                    taskList.remove(0);
                    drawNum++;


                    //时间周期到了并且未在调整中，则开启调整
//                    logger.info("间隔时间：" + (currTime - point) + " flag:" + isInAdaptflag);
                    if (currTime - point > 150 && isInAdaptflag == 0) {
                        lambda_now = DataProcess.format(count / (currTime - point), 2);
                        if (lambda_pre == 0) {                //第一个周期不调整
                            lambda_pre = lambda_now;
                        } else {
                            //密度变化超过0.7，开启调整
                            if (lambda_now - lambda_pre > 1 || lambda_now - lambda_pre < -1) {
                                logger.info("调整开始,当前时间：" + currTime + " lambda_now:" + lambda_now + " lambda_pre:" + lambda_pre);
                                if (lambda_now > lambda_pre) {
                                    ListUtil.moveHeadToTail(beforeMECList, afterMECList, a);
                                    logger.info("lambda变大，右移动" + beforeMECList.size());
                                } else {
                                    if(left == 0){  //第一次左移 步长为4
                                        a = 4;
                                        left = 1;
                                    }
                                    ListUtil.moveTailToHead(beforeMECList, afterMECList, a);
                                    logger.info("lambda变小，左移" + beforeMECList.size());
                                }
                                logger.info("移动后theta: " + beforeMECList.size() / 30.0);
                                a /= 2;
                                isInAdaptflag = 1;
                                lastPoint = currTime;   //小周期开始计时
                            } else {
                                //不满足调整条件，count和point重置
                                point = currTime;
                                count = 0;
                                lambda_pre = lambda_now;
                            }
                        }
                    }
                    //在调整中，控制步长
                    if (isInAdaptflag == 1) {
                        interval = currTime - lastPoint;
                        if (interval >= 80) {    //达到小周期
                            double ECR = DataProcess.format((double) (interval_ugtaskNum - interval_failUgTaskNum) * 100 / interval_ugtaskNum, 2);
                            logger.info("小周期满，currentTime：" + currTime + " lastPoint: " + lastPoint + " 成功率: " + ECR);
                            if ((ECR < 84 || ECR > 90) && beforeMECList.size() >= 2 && afterMECList.size() >= 2) {
                                if (lambda_now > lambda_pre) {
                                    logger.info("a="+a);
                                    ListUtil.moveHeadToTail(beforeMECList, afterMECList, a);
                                    logger.info("右移动" + beforeMECList.size());
                                } else {
                                    ListUtil.moveTailToHead(beforeMECList, afterMECList, a);
                                    logger.info("左移动" + beforeMECList.size());
                                }
                                logger.info("移动后theta: " + beforeMECList.size() / 30.0);
                                if (a >= 2) {
                                    a /= 2;
                                } else {
                                    a = 1;
                                }
                            } else {    //本次调整结束
                                a = 1;
                                isInAdaptflag = 0;
                                lambda_pre = lambda_now;
                                count = 0;
                                point = currTime;
                                logger.info("调整结束，当前时间：" + currTime + " theta值为：" + beforeMECList.size() / 30.0 + " 成功率为：" + ECR);
                            }
                            lastPoint = currTime;
                            interval_ugtaskNum = 0;
                            interval_failUgTaskNum = 0;

                        }
                    }


                    if (currTime - resPoint > 150) {
                        res_lambda = DataProcess.format(res_count / (currTime - resPoint), 2);
                        ecr = DataProcess.format((double) (res_etaskNum - res_failEtaskNum) * 100 / res_etaskNum, 2);
                        res_theta = DataProcess.format(beforeMECList.size() / 30.0, 2);
                        res_ntaskTime = DataProcess.format((paperSts.totalNormalDelay - last_paperSts.totalNormalDelay) / (paperSts.normalTask - last_paperSts.normalTask), 2);
                        lamda_list.add(res_lambda);
                        theta_list.add(res_theta);
                        ecr_list.add(ecr);
                        nat_list.add(res_ntaskTime);

                        logger.info("当前时间：" + currTime + " 统计结果：" + res_lambda + " " + res_theta + " " + ecr + "% " + res_ntaskTime);

                        res_etaskNum = 0;
                        res_failEtaskNum = 0;
                        res_count = 0;
                        resPoint = currTime;
                        last_paperSts.totalNormalDelay = paperSts.totalNormalDelay;
                        last_paperSts.normalTask = paperSts.normalTask;
                    }


                }
                timeFlies();    // 时间流逝
                continue;
            }

        }

        if (normalTasks.size() > 0) {   //遗传算法触发条件
            int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
            // 分配任务
            for (int i = 0; i < assginStrategy.length; i++) {
                MEC chooseMec = afterMECList.get(assginStrategy[i]);
                chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
            }
            normalTasks.clear();
        }


        while (!isAllTaskFinish(beforeMECList) || !isAllTaskFinish(afterMECList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : beforeMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            for (MEC mec : afterMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            timeFlies();
        }


        double cRate = DataProcess.format((paperSts.urgencyTask - failNum) * 100 / (double) paperSts.urgencyTask, 2);   // 紧急任务成功率
        double tDelay = DataProcess.format(paperSts.totalNormalDelay / paperSts.normalTask, 2); // 普通任务平均时延

        logger.info("");
        logger.info("直接执行：" + directNum);
        logger.info("重组执行：" + rePlaceNum);
        logger.info("临时抢占：" + preemptNum);
        logger.info("卸载失败：" + failNum);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalN);
        logger.info("移除普通任务数：" + paperSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rcr.setPaperCRate(cRate);
        rtd.setPaperTDelay(tDelay);


        logger.info("================================");
        for (Double aDouble : lamda_list) {
            logger.info("lambda=" + aDouble);
        }
        for (Double aDouble : theta_list) {
            logger.info("theta=" + aDouble);
        }
        for (Double aDouble : ecr_list) {
            logger.info("ecr=" + aDouble);
        }
        for (Double aDouble : nat_list) {
            logger.info("nat=" + aDouble);
        }

    }


    public static void paperScheduleOndifTheta(List<Task> tasks, double theta, ResCompleteRate rcr, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        // 获取任务集和MEC集
        List<Task> taskList = DeepCopy.deepCopy(tasks);
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();

        System.out.print("Progress:");
        Statistics paperSts = new Statistics(); // 统计信息对象
        int directNum = 0, rePlaceNum = 0, failNum = 0, preemptNum = 0;
        double calMount = 0; // 紧急任务的计算量
        int task45_5 = 0, task4_45 = 0;

        int totalN = taskList.size();
        int drawNum = 0, index = 0;
        String finish = getNChar(index / BITE, '█');
        String unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');
        String target = String.format("%3d%%[%s%s]", index, finish, unFinish);
        System.out.print(target);

        // 按照计算能力划分MEC
        Collections.sort(mecList, (t1, t2) -> t1.getC() - t2.getC() < 0 ? 1 : (t1.getC() - t2.getC() == 0) ? 0 : -1);
        int thetaNum = (int) (theta * mecList.size());
        List<MEC> beforeMECList = new ArrayList<>(mecList.subList(0, thetaNum));   // subList为视图而非实体对象，无法深拷贝
        List<MEC> afterMECList = new ArrayList<>(mecList.subList(thetaNum, mecList.size()));


        //调度开始
        List<Task> normalTasks = new ArrayList<>();     // 普通任务队列，采用遗传算法调度
        int popSize = 150;  // 合并种群大小
        while (taskList.size() > 0) {
            if (taskList.get(0).getArriveTime() > currTime) {
                timeFlies();    // 时间流逝
                continue;
            } else {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    // 移除所有MEC中已经完成的任务
                    for (MEC mec : beforeMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }
                    for (MEC mec : afterMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }

                    Task taskNow = taskList.get(0);

                    if (taskNow.getUrgency() != 3) {  // 紧急任务单任务分配
                        paperSts.urgencyTask++;
                        // 选取最优的MEC
                        MEC bestMEC = DESAlgorithm.getBestMEC(beforeMECList, taskNow, currTime);
                        if (bestMEC != null) {
                            bestMEC.acceptAndUpdateTime(taskNow, currTime);
                            directNum++;
                            calMount += taskNow.getL();
                            if (taskNow.getL() >= 4.5) {
                                task45_5++;
                            } else if (taskNow.getL() >= 4) {
                                task4_45++;
                            }
                        } else {
                            // 获取迁移方案
                            RegroupParam replaceRes = DESAlgorithm.regroupTask(beforeMECList, taskNow, currTime);
                            if (replaceRes != null) {
                                MEC selectMEC = null;
                                for (MEC mec : beforeMECList) {
                                    if (mec.getId() == replaceRes.getMec().getId()) {
                                        selectMEC = mec;
                                    }
                                }
                                // 先将选中的MEC任务按迁移策略迁移
                                for (Map.Entry entry : replaceRes.getTransfer().entrySet()) {
                                    Task task = (Task) entry.getKey();
                                    selectMEC.remove(task);
                                    selectMEC.updateTimeAfterRemove();

                                    MEC transferMEC = null;
                                    for (MEC mec : beforeMECList) {
                                        if (mec.getId() == replaceRes.getTransfer().get(task).getId()) {
                                            transferMEC = mec;
                                        }
                                    }
                                    transferMEC.acceptAndUpdateTime(task, currTime);
                                }

                                // 任务执行
                                selectMEC.acceptAndUpdateTime(taskNow, currTime);
                                rePlaceNum++;
                                calMount += taskNow.getL();
                                if (taskNow.getL() >= 4.5) {
                                    task45_5++;
                                } else if (taskNow.getL() >= 4) {
                                    task4_45++;
                                }
                            } else {
                                MEC tmpUseMEC = DESAlgorithm.getBestMEC(afterMECList, taskNow, currTime);
                                if (tmpUseMEC != null) {
                                    tmpUseMEC.acceptAndUpdateTime(taskNow, currTime);
                                    preemptNum++;
                                    calMount += taskNow.getL();
                                    if (taskNow.getL() >= 4.5) {
                                        task45_5++;
                                    } else if (taskNow.getL() >= 4) {
                                        task4_45++;
                                    }
                                } else {
                                    failNum++;
                                }
                            }
                        }
                    } else {  // 非紧急任务加入到遗传算法的调度队列
                        normalTasks.add(taskNow);
                        if (normalTasks.size() == 10) {   //遗传算法触发条件
                            {
                                int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
//                                logger.info("分配策略："+ Arrays.toString(assginStrategy));
                                // 分配任务
                                for (int i = 0; i < assginStrategy.length; i++) {
                                    MEC chooseMec = afterMECList.get(assginStrategy[i]);
                                    chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
                                }
                                normalTasks.clear();
                                interval = 0;
                            }
                        }
                    }
                    taskList.remove(0);
                    drawNum++;
                }

                finish = getNChar(index / BITE, '█');
                unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');

                target = String.format("%3d%%├%s%s┤", index, finish, unFinish);
                System.out.print(getNChar(PROGRESS_SIZE + 6, '\b'));
                System.out.print(target);
                index = (int) (1 + 100.0 * drawNum / totalN);


                timeFlies();    // 时间流逝
                continue;
            }

        }

        if (normalTasks.size() > 0) {   //遗传算法触发条件
            int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
            // 分配任务
            for (int i = 0; i < assginStrategy.length; i++) {
                MEC chooseMec = afterMECList.get(assginStrategy[i]);
                chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
            }
            normalTasks.clear();
        }


        while (!isAllTaskFinish(beforeMECList) || !isAllTaskFinish(afterMECList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : beforeMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            for (MEC mec : afterMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            timeFlies();
        }


        double cRate = DataProcess.format((paperSts.urgencyTask - failNum) * 100 / (double) paperSts.urgencyTask, 2);   // 紧急任务成功率
        double tDelay = DataProcess.format(paperSts.totalNormalDelay / paperSts.normalTask, 2); // 普通任务平均时延

        logger.info("");
        logger.info("直接执行：" + directNum);
        logger.info("重组执行：" + rePlaceNum);
        logger.info("临时抢占：" + preemptNum);
        logger.info("卸载失败：" + failNum);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalN);
        logger.info("移除普通任务数：" + paperSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rcr.setPaperCRate(cRate);
        rtd.setPaperTDelay(tDelay);
    }


    /**
     * 本文调度算法在不同MEC资源划分阈值下
     *
     * @param inputVals 任务总集
     * @param m         第m个任务列表
     * @param rcr       结果保存对象
     */
    public static void paperScheduleOnDifferentTheta(List<InputVal> inputVals, int m, double theta, ResCompleteRate rcr, ResTotalDelay rtd) {
        currTime = 0;   // 重置时间
        // 获取任务集和MEC集
        List<Task> taskList = DeepCopy.deepCopy(inputVals.get(m).getTaskList());
        FileHelper mecFile = new FileHelper(DataProcess.MECLISTPATH);
        List<MEC> mecList = mecFile.getObjFromFile();

        System.out.print("Progress:");
        Statistics paperSts = new Statistics(); // 统计信息对象
        int directNum = 0, rePlaceNum = 0, failNum = 0, preemptNum = 0;
        double calMount = 0; // 紧急任务的计算量
        int task45_5 = 0, task4_45 = 0;

        int totalN = taskList.size();
        int drawNum = 0, index = 0;
        String finish = getNChar(index / BITE, '█');
        String unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');
        String target = String.format("%3d%%[%s%s]", index, finish, unFinish);
        System.out.print(target);

        // 按照计算能力划分MEC
        Collections.sort(mecList, (t1, t2) -> t1.getC() - t2.getC() < 0 ? 1 : (t1.getC() - t2.getC() == 0) ? 0 : -1);
        int thetaNum = (int) (theta * mecList.size());
        List<MEC> beforeMECList = new ArrayList<>(mecList.subList(0, thetaNum));   // subList为视图而非实体对象，无法深拷贝
        List<MEC> afterMECList = new ArrayList<>(mecList.subList(thetaNum, mecList.size()));


        //调度开始
        List<Task> normalTasks = new ArrayList<>();     // 普通任务队列，采用遗传算法调度
        int popSize = 180;  // 合并种群大小
        while (taskList.size() > 0) {
            if (taskList.get(0).getArriveTime() > currTime) {
                timeFlies();    // 时间流逝
                continue;
            } else {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    // 移除所有MEC中已经完成的任务
                    for (MEC mec : beforeMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }
                    for (MEC mec : afterMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }

                    Task taskNow = taskList.get(0);

                    if (taskNow.getUrgency() != 3) {  // 紧急任务单任务分配
                        paperSts.urgencyTask++;
                        // 选取最优的MEC
                        MEC bestMEC = DESAlgorithm.getBestMEC(beforeMECList, taskNow, currTime);
                        if (bestMEC != null) {
                            bestMEC.acceptAndUpdateTime(taskNow, currTime);
                            directNum++;
                            calMount += taskNow.getL();
                            if (taskNow.getL() >= 4.5) {
                                task45_5++;
                            } else if (taskNow.getL() >= 4) {
                                task4_45++;
                            }
                        } else {
                            // 获取迁移方案
                            RegroupParam replaceRes = DESAlgorithm.regroupTask(beforeMECList, taskNow, currTime);
                            if (replaceRes != null) {
                                MEC selectMEC = null;
                                for (MEC mec : beforeMECList) {
                                    if (mec.getId() == replaceRes.getMec().getId()) {
                                        selectMEC = mec;
                                    }
                                }
                                // 先将选中的MEC任务按迁移策略迁移
                                for (Map.Entry entry : replaceRes.getTransfer().entrySet()) {
                                    Task task = (Task) entry.getKey();
                                    selectMEC.remove(task);
                                    selectMEC.updateTimeAfterRemove();

                                    MEC transferMEC = null;
                                    for (MEC mec : beforeMECList) {
                                        if (mec.getId() == replaceRes.getTransfer().get(task).getId()) {
                                            transferMEC = mec;
                                        }
                                    }
                                    transferMEC.acceptAndUpdateTime(task, currTime);
                                }

                                // 任务执行
                                selectMEC.acceptAndUpdateTime(taskNow, currTime);
                                rePlaceNum++;
                                calMount += taskNow.getL();
                                if (taskNow.getL() >= 4.5) {
                                    task45_5++;
                                } else if (taskNow.getL() >= 4) {
                                    task4_45++;
                                }
                            } else {
                                MEC tmpUseMEC = DESAlgorithm.getBestMEC(afterMECList, taskNow, currTime);
                                if (tmpUseMEC != null) {
                                    tmpUseMEC.acceptAndUpdateTime(taskNow, currTime);
                                    preemptNum++;
                                    calMount += taskNow.getL();
                                    if (taskNow.getL() >= 4.5) {
                                        task45_5++;
                                    } else if (taskNow.getL() >= 4) {
                                        task4_45++;
                                    }
                                } else {
                                    failNum++;
                                }
                            }
                        }
                    } else {  // 非紧急任务加入到遗传算法的调度队列
                        normalTasks.add(taskNow);
                        if (normalTasks.size() == 10) {   //遗传算法触发条件
                            {
                                int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
//                                logger.info("分配策略："+ Arrays.toString(assginStrategy));
                                // 分配任务
                                for (int i = 0; i < assginStrategy.length; i++) {
                                    MEC chooseMec = afterMECList.get(assginStrategy[i]);
                                    chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
                                }
                                normalTasks.clear();
                                interval = 0;
                            }
                        }
                    }
                    taskList.remove(0);
                    drawNum++;
                }

                finish = getNChar(index / BITE, '█');
                unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');

                target = String.format("%3d%%├%s%s┤", index, finish, unFinish);
                System.out.print(getNChar(PROGRESS_SIZE + 6, '\b'));
                System.out.print(target);
                index = (int) (1 + 100.0 * drawNum / totalN);


                timeFlies();    // 时间流逝
                continue;
            }

        }

        if (normalTasks.size() > 0) {   //遗传算法触发条件
            int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
            // 分配任务
            for (int i = 0; i < assginStrategy.length; i++) {
                MEC chooseMec = afterMECList.get(assginStrategy[i]);
                chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
            }
            normalTasks.clear();
        }


        while (!isAllTaskFinish(beforeMECList) || !isAllTaskFinish(afterMECList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : beforeMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            for (MEC mec : afterMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            timeFlies();
        }


        double cRate = DataProcess.format((paperSts.urgencyTask - failNum) * 100 / (double) paperSts.urgencyTask, 2);   // 紧急任务成功率
        double tDelay = DataProcess.format(paperSts.totalNormalDelay / paperSts.normalTask, 2); // 普通任务平均时延

        logger.info("");
        logger.info("直接执行：" + directNum);
        logger.info("重组执行：" + rePlaceNum);
        logger.info("临时抢占：" + preemptNum);
        logger.info("卸载失败：" + failNum);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalN);
        logger.info("移除普通任务数：" + paperSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        rcr.setPaperCRate(cRate);
        rtd.setPaperTDelay(tDelay);
    }


    /**
     * 本文调度算法在不同遗传算法触发阈值下
     *
     * @param rcr 结果保存对象
     */
    public static void paperScheduleOnDifferentGATheta(List<Task> otaskList, List<MEC> omecList, int gaTheta, ResCompleteRate rcr, ResTotalDelay rtd, List<Double> gaTimeList) {
        double gaUseTime = 0;
        long startTime = System.currentTimeMillis();
        currTime = 0;   // 重置时间
        // 获取任务集和MEC集
        List<Task> taskList = DeepCopy.deepCopy(otaskList);
        List<MEC> mecList = DeepCopy.deepCopy(omecList);

        System.out.print("Progress:");
        Statistics paperSts = new Statistics(); // 统计信息对象
        int directNum = 0, rePlaceNum = 0, failNum = 0, preemptNum = 0;
        double calMount = 0; // 紧急任务的计算量
        int task45_5 = 0, task4_45 = 0;

        int totalN = taskList.size();
        int drawNum = 0, index = 0;
        String finish = getNChar(index / BITE, '█');
        String unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');
        String target = String.format("%3d%%[%s%s]", index, finish, unFinish);
        System.out.print(target);

        // 按照计算能力划分MEC
        Collections.sort(mecList, (t1, t2) -> t1.getC() - t2.getC() < 0 ? 1 : (t1.getC() - t2.getC() == 0) ? 0 : -1);
        int thetaNum = (int) (0.4 * mecList.size());
        List<MEC> beforeMECList = new ArrayList<>(mecList.subList(0, thetaNum));   // subList为视图而非实体对象，无法深拷贝
        List<MEC> afterMECList = new ArrayList<>(mecList.subList(thetaNum, mecList.size()));


        //调度开始
        List<Task> normalTasks = new ArrayList<>();     // 普通任务队列，采用遗传算法调度
        int popSize = 180;  // 合并种群大小
        while (taskList.size() > 0) {
            if (taskList.get(0).getArriveTime() > currTime) {
                timeFlies();    // 时间流逝
                continue;
            } else {
                while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                    // 移除所有MEC中已经完成的任务
                    for (MEC mec : beforeMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }
                    for (MEC mec : afterMECList) {
                        mec.removeCompletedTasks(currTime, paperSts);
                    }

                    Task taskNow = taskList.get(0);

                    if (taskNow.getUrgency() != 3) {  // 紧急任务单任务分配
                        paperSts.urgencyTask++;
                        // 选取最优的MEC
                        MEC bestMEC = DESAlgorithm.getBestMEC(beforeMECList, taskNow, currTime);
                        if (bestMEC != null) {
                            bestMEC.acceptAndUpdateTime(taskNow, currTime);
                            directNum++;
                            calMount += taskNow.getL();
                            if (taskNow.getL() >= 4.5) {
                                task45_5++;
                            } else if (taskNow.getL() >= 4) {
                                task4_45++;
                            }
                        } else {
                            // 获取迁移方案
                            RegroupParam replaceRes = DESAlgorithm.regroupTask(beforeMECList, taskNow, currTime);
                            if (replaceRes != null) {
                                MEC selectMEC = null;
                                for (MEC mec : beforeMECList) {
                                    if (mec.getId() == replaceRes.getMec().getId()) {
                                        selectMEC = mec;
                                    }
                                }
                                // 先将选中的MEC任务按迁移策略迁移
                                for (Map.Entry entry : replaceRes.getTransfer().entrySet()) {
                                    Task task = (Task) entry.getKey();
                                    selectMEC.remove(task);
                                    selectMEC.updateTimeAfterRemove();

                                    MEC transferMEC = null;
                                    for (MEC mec : beforeMECList) {
                                        if (mec.getId() == replaceRes.getTransfer().get(task).getId()) {
                                            transferMEC = mec;
                                        }
                                    }
                                    transferMEC.acceptAndUpdateTime(task, currTime);
                                }

                                // 任务执行
                                selectMEC.acceptAndUpdateTime(taskNow, currTime);
                                rePlaceNum++;
                                calMount += taskNow.getL();
                                if (taskNow.getL() >= 4.5) {
                                    task45_5++;
                                } else if (taskNow.getL() >= 4) {
                                    task4_45++;
                                }
                            } else {
                                MEC tmpUseMEC = DESAlgorithm.getBestMEC(afterMECList, taskNow, currTime);
                                if (tmpUseMEC != null) {
                                    tmpUseMEC.acceptAndUpdateTime(taskNow, currTime);
                                    preemptNum++;
                                    calMount += taskNow.getL();
                                    if (taskNow.getL() >= 4.5) {
                                        task45_5++;
                                    } else if (taskNow.getL() >= 4) {
                                        task4_45++;
                                    }
                                } else {
                                    failNum++;
                                }
                            }
                        }
                    } else {  // 非紧急任务加入到遗传算法的调度队列
                        normalTasks.add(taskNow);
                        if (normalTasks.size() == gaTheta) {   //遗传算法触发条件
                            long startTime2 = System.currentTimeMillis();
                            {
                                int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
//                                logger.info("分配策略："+ Arrays.toString(assginStrategy));
                                // 分配任务
                                for (int i = 0; i < assginStrategy.length; i++) {
                                    MEC chooseMec = afterMECList.get(assginStrategy[i]);
                                    chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
                                }
                                normalTasks.clear();
                                interval = 0;
                            }
                            long endTime2 = System.currentTimeMillis();
                            gaUseTime += (endTime2 - startTime2) / 1000.0;
                        }
                    }
                    taskList.remove(0);
                    drawNum++;
                }

                finish = getNChar(index / BITE, '█');
                unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');

                target = String.format("%3d%%├%s%s┤", index, finish, unFinish);
                System.out.print(getNChar(PROGRESS_SIZE + 6, '\b'));
                System.out.print(target);
                index = (int) (1 + 100.0 * drawNum / totalN);

                timeFlies();    // 时间流逝
                continue;
            }

        }

        long startTime2 = System.currentTimeMillis();
        if (normalTasks.size() > 0) {   //遗传算法触发条件
            int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
            // 分配任务
            for (int i = 0; i < assginStrategy.length; i++) {
                MEC chooseMec = afterMECList.get(assginStrategy[i]);
                chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
            }
            normalTasks.clear();
        }
        long endTime2 = System.currentTimeMillis();
        gaUseTime += (endTime2 - startTime2) / 1000.0;


        while (!isAllTaskFinish(beforeMECList) || !isAllTaskFinish(afterMECList)) {
            // 移除所有MEC中已经完成的任务
            for (MEC mec : beforeMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            for (MEC mec : afterMECList) {
                mec.removeCompletedTasks(currTime, paperSts);
            }
            timeFlies();
        }
        gaTimeList.add(gaUseTime);
        long endTime = System.currentTimeMillis();
        long usedTime = (endTime - startTime) / 1000;

        double cRate = DataProcess.format((paperSts.urgencyTask - failNum) * 100 / (double) paperSts.urgencyTask, 2);   // 紧急任务成功率
        double tDelay = DataProcess.format(paperSts.totalNormalDelay / paperSts.normalTask, 2); // 普通任务平均时延

        logger.info("");
        logger.info("直接执行：" + directNum);
        logger.info("重组执行：" + rePlaceNum);
        logger.info("临时抢占：" + preemptNum);
        logger.info("卸载失败：" + failNum);
        logger.info("成功率：" + cRate);
        logger.info("总任务数：" + totalN);
        logger.info("移除普通任务数：" + paperSts.normalTask);
        logger.info("普通任务平均时延：" + tDelay);
        logger.info("算法执行时间：" + usedTime);
        logger.info("普通任务执行时间：" + gaUseTime);
        rcr.setPaperCRate(cRate);
        rtd.setPaperTDelay(tDelay);
    }

    public static void timeFlies() {
        currTime = DataProcess.format(currTime + 0.01, 2);
        interval = DataProcess.format(interval + 0.01, 2);

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
