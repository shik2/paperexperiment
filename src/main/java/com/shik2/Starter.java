/*
package com.shik2;

import com.shik2.compare.CloseAlgorithm;
import com.shik2.compare.GAAlgorithm;
import com.shik2.earliest.DESAlgorithm;
import com.shik2.earliest.RegroupParam;
import com.shik2.earliest.Task;
import com.shik2.utils.DataProcess;
import com.shik2.utils.FileHelper;
import com.shik2.ga.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

*/
/**
 * @author shik2
 * @date 2020/12/19
 * <p>
 * Description:
 **//*

public class Starter {

    static double currTime = 0;
    static double interval = 0;  // 两次遗传算法间隔
    private static Logger logger = LoggerFactory.getLogger(Starter.class);


    private int index = 0;
    private String finish;
    private String unFinish;


    // 进度条粒度
    private final int PROGRESS_SIZE = 50;
    private int BITE = 2;


    public static void main(String[] args) {


//        TaskAndMECProduce.generateTasksAndMECs(15, 1000);
        {
            // 获取任务集和MEC集
            FileHelper taskFile = new FileHelper("C:\\Users\\Administrator\\Desktop\\taskList.txt");
            List<Task> taskList = taskFile.getObjFromFile();
            FileHelper mecFile = new FileHelper("C:\\Users\\Administrator\\Desktop\\mecList.txt");
            List<MEC> mecList = mecFile.getObjFromFile();
            // 按照计算能力划分MEC
            Collections.sort(mecList, (t1, t2) -> t1.getC() - t2.getC() < 0 ? 1 : (t1.getC() - t2.getC() == 0) ? 0 : -1);
            int theta = (int) (0.8 * mecList.size());
            List<MEC> beforeMECList = new ArrayList<>(mecList.subList(0, theta));   // subList为视图而非实体对象，无法深拷贝
            List<MEC> afterMECList = new ArrayList<>(mecList.subList(theta, mecList.size()));

            logger.info("最强MEC：" + beforeMECList.get(0).getId() + "， 计算能力：" + beforeMECList.get(0).getC());

            //调度开始
            List<Task> normalTasks = new ArrayList<>();     // 普通任务队列，采用遗传算法调度
            int popSize = 180;  // 合并种群大小
            while (taskList.size() > 0) {
                if (taskList.get(0).getArriveTime() > currTime) {
                    timeFlies();    // 时间流逝
                    continue;
                } else {
                    while (taskList.size() > 0 && taskList.get(0).getArriveTime() == currTime) {
                        System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
                        // 移除所有MEC中已经完成的任务
                        logger.info("当前时间为：" + currTime);
                        for (MEC mec : beforeMECList) {
                            mec.removeCompletedTasks(currTime);
                        }
                        for (MEC mec : afterMECList) {
                            mec.removeCompletedTasks(currTime);
                        }

                        Task taskNow = taskList.get(0);
                        logger.warn("任务" + taskNow.getId() + "到达，到达时间：" + taskNow.getArriveTime() + ", 紧急度：" + taskNow.getUrgency() + ", 任务量：" + taskNow.getL() + ", DL=" + taskNow.getDeadline());

                        if (taskNow.getUrgency() != 3) {  // 紧急任务单任务分配
                            // 选取最优的MEC
                            logger.warn("阶段一：开始为任务选取最优的直接执行MEC");
                            MEC bestMEC = DESAlgorithm.getBestMEC(beforeMECList, taskNow, currTime);
                            if (bestMEC != null) {
                                bestMEC.acceptAndUpdateTime(taskNow, currTime);
                                logger.info("任务" + taskNow.getId() + "由ECS" + bestMEC.getId() + "直接执行, 计算能力：" + bestMEC.getC());
                                logger.info("插入后任务列表为： " + bestMEC.getTaskList());
                            } else {
                                logger.info("任务" + taskNow.getId() + "找不到可以直接运行任务的ECS");
                                logger.warn("阶段二：直接执行失败，开始执行任务替换策略");
                                // 获取迁移方案
                                RegroupParam replaceRes = DESAlgorithm.regroupTask(beforeMECList, taskNow, currTime);
                                if (replaceRes != null) {
                                    MEC selectMEC = null;
                                    for (MEC mec : beforeMECList) {
                                        if (mec.getId() == replaceRes.getMec().getId()) {
                                            selectMEC = mec;
                                        }
                                    }
                                    logger.info("迁移前的任务列表: " + selectMEC.getTaskList());
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
                                        logger.info("接收任务" + task.getId() + "的MEC原任务列表：" + transferMEC.getTaskList());
                                        transferMEC.acceptAndUpdateTime(task, currTime);
                                        logger.info(task.getId() + " 由ECS" + transferMEC.getId() + "迁移执行");
                                        logger.info("接收任务" + task.getId() + "的MEC现任务列表：" + transferMEC.getTaskList());
                                    }

                                    logger.info("MEC" + selectMEC.getId() + "迁移完成后的任务列表：" + selectMEC.getTaskList());
                                    // 任务执行
                                    selectMEC.acceptAndUpdateTime(taskNow, currTime);
                                    logger.info(taskNow.getId() + " 经过替换策略由ECS" + selectMEC.getId() + "执行");
                                    logger.info("最终任务列表：" + selectMEC.getTaskList());
                                } else {
                                    logger.warn("阶段三：任务替换策略失败，判断是否可以临时占用普通任务的MEC");
                                    MEC tmpUseMEC = DESAlgorithm.getBestMEC(afterMECList, taskNow, currTime);
                                    if (tmpUseMEC != null) {
                                        logger.info("临时占用MEC-" + tmpUseMEC.getId());
                                        tmpUseMEC.acceptAndUpdateTime(taskNow, currTime);
                                    } else {
                                        logger.warn("任务" + taskNow.getId() + "无法按时执行");
                                        logger.info("任务信息：" + taskNow);
                                    }
                                }
                            }
                        } else {  // 非紧急任务加入到遗传算法的调度队列
                            normalTasks.add(taskNow);
                            logger.info("加入遗传算法调度队列");
                            if (normalTasks.size() == 10) {   //遗传算法触发条件
                                {
                                    logger.warn("任务队列长度达标触发遗传算法，间隔：" + interval);
                                    int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
                                    logger.info("进化结束!, 最佳调度策略：" + Arrays.toString(assginStrategy));
                                    // 分配任务
                                    for (int i = 0; i < assginStrategy.length; i++) {
                                        MEC chooseMec = afterMECList.get(assginStrategy[i]);
                                        System.out.println(chooseMec);
//                                    logger.info("分配前：" + chooseMec.getTaskList());
                                        System.out.println(normalTasks.get(i));
                                        chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
//                                    logger.info("分配后：" + chooseMec.getTaskList());
                                    }

                                    normalTasks.clear();
                                    interval = 0;
                                }
                            }
                        }

                        taskList.remove(0);
                    }
                    timeFlies();    // 时间流逝
                    continue;
                }

            }


            if (normalTasks.size() > 0) {   //遗传算法触发条件
                logger.warn("普通队列还有任务，触发最终遗传算法");
                int[] assginStrategy = getGaStrategy(afterMECList, normalTasks, popSize);
                logger.info("进化结束!, 最佳调度策略：" + Arrays.toString(assginStrategy));
                // 分配任务
                for (int i = 0; i < assginStrategy.length; i++) {
                    MEC chooseMec = afterMECList.get(assginStrategy[i]);
                    System.out.println(chooseMec);
                    System.out.println(normalTasks.get(i));
                    chooseMec.gaAcceptAndUpdateTime(normalTasks.get(i), currTime);
                }
                normalTasks.clear();
            }

            logger.info("================================================================");

        }

   */
/*     {
            // 卸载到最近的MEC
            FileHelper taskFile2 = new FileHelper("C:\\Users\\Administrator\\Desktop\\taskList.txt");
            List<Task> taskList2 = taskFile2.getObjFromFile();
            FileHelper mecFile2 = new FileHelper("C:\\Users\\Administrator\\Desktop\\mecList.txt");
            List<MEC> mecList2 = mecFile2.getObjFromFile();


            for (Task task : taskList2) {
                if (task.getUrgency() == 3) {
                    task.setDeadline(task.getDlClosest());
                }
            }

            currTime = 0;   // 重置时间
            int acceptTaskNum = 0, urgencyTaskNum = 0, totalTaskNum = taskList2.size();
            //调度开始
            while (taskList2.size() > 0) {
                // 时间流逝
                if (!(taskList2.get(0).getArriveTime() > currTime)) {
                    while (taskList2.size() > 0 && taskList2.get(0).getArriveTime() == currTime) {
//                        System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
                        // 移除所有MEC中已经完成的任务
//                        logger.info("当前时间为：" + currTime);
                        for (MEC mec : mecList2) {
                            mec.removeCompletedTasks(currTime);
                        }

                        Task taskNow = taskList2.get(0);
//                        logger.warn("任务" + taskNow.getId() + "到达，到达时间：" + taskNow.getArriveTime() + ", 紧急度：" + taskNow.getUrgency() + ", 任务量：" + taskNow.getL() + ", DL=" + taskNow.getDeadline());

                        // 最近的MEC
                        MEC closeestMEC = mecList2.get(taskNow.getClosestMEC());

                        if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                            urgencyTaskNum++;
                        }

                        if (CloseAlgorithm.acceptTask(closeestMEC, taskNow, currTime)) {
                            if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                                acceptTaskNum++;
                            }
                        }
                        taskList2.remove(0);
                    }
                }
                timeFlies();    // 时间流逝
                continue;

            }
            logger.info("================================================================");
            logger.info("完成紧急任务数：" + acceptTaskNum);
            logger.info("总共紧急任务数：" + urgencyTaskNum);
            logger.info("总任务数：" + totalTaskNum);
            logger.info("成功率：" + DataProcess.format(acceptTaskNum * 100 / urgencyTaskNum, 2));
        }*//*



        {
            FileHelper taskFile3 = new FileHelper("C:\\Users\\Administrator\\Desktop\\taskList.txt");
            List<Task> taskList3 = taskFile3.getObjFromFile();
            FileHelper mecFile3 = new FileHelper("C:\\Users\\Administrator\\Desktop\\mecList.txt");
            List<MEC> mecList3 = mecFile3.getObjFromFile();


            for (Task task : taskList3) {
                if (task.getUrgency() == 3) {
                    task.setDeadline(task.getDlClosest());
                }
            }

            currTime = 0;   // 重置时间
            List<Task> normalTasks = new ArrayList<>();     // 遗传算法调度队列
            int popSize = 180;
            int acceptTaskNum = 0, urgencyTaskNum = 0, totalTaskNum = taskList3.size();
            //调度开始
            while (taskList3.size() > 0) {
                // 时间流逝
                if (!(taskList3.get(0).getArriveTime() > currTime)) {
                    while (taskList3.size() > 0 && taskList3.get(0).getArriveTime() == currTime) {
                        // 移除完成的任务
                        for (MEC mec : mecList3) {
                            mec.removeCompletedTasks(currTime);
                        }

                        Task taskNow = taskList3.get(0);
                        normalTasks.add(taskNow);

                        if (taskNow.getUrgency() == 1 || taskNow.getUrgency() == 2) {
                            urgencyTaskNum++;
                        }

                        if (normalTasks.size() == 10) {   //遗传算法触发条件
                            int[] assginStrategy = getGaStrategy(mecList3, normalTasks, popSize);
                            logger.info("分配策略：" + Arrays.toString(assginStrategy));
                            // 分配任务
                            for (int i = 0; i < assginStrategy.length; i++) {
                                MEC chooseMec = mecList3.get(assginStrategy[i]);
                                Task assginTask = normalTasks.get(i);
//                                logger.info("当前任务：" + assginTask + " 当前时间：" + currTime);
//                                logger.info("选择的MEC：" + chooseMec);
//                                logger.info("分配前的任务列表：" + chooseMec.getTaskList());
                                if (GAAlgorithm.acceptTask(chooseMec, assginTask, currTime)) {
//                                    logger.info("满足条件，分配成功");
                                    if (assginTask.getUrgency() == 1 || assginTask.getUrgency() == 2) {
                                        acceptTaskNum++;
                                    }
                                } else {
//                                    logger.info("分配失败");
                                }
//                                logger.info("分配后的任务列表：" + chooseMec.getTaskList());
                            }
                            normalTasks.clear();
                            interval = 0;
                        }
                        taskList3.remove(0);
                    }
                }
                timeFlies();    // 时间流逝
                continue;

            }
            logger.info("================================================================");
            logger.info("完成紧急任务数：" + acceptTaskNum);
            logger.info("总共紧急任务数：" + urgencyTaskNum);
            logger.info("总任务数：" + totalTaskNum);
            logger.info("成功率：" + DataProcess.format(acceptTaskNum * 100 / urgencyTaskNum, 2));
        }


    }


    public static void timeFlies() {
        currTime = DataProcess.format(currTime + 0.01, 2);
        interval = DataProcess.format(interval + 0.01, 2);

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
//        logger.info("初始种群适应度最佳为： " + maxFitness);
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

}
*/
