package com.shik2.earliest;

import com.shik2.ga.MEC;
import com.shik2.utils.DeepCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author shik2
 * @date 2020/12/21
 * <p>
 * Description:
 **/
public class DESAlgorithm {

    private static Logger logger = LoggerFactory.getLogger(DESAlgorithm.class);

    /**
     * DES直接执行策略：找到最佳的MEC
     *
     * @param beforeMECList
     * @param newTask
     * @param currTime
     * @return
     */
    public static MEC getBestMEC(List<MEC> beforeMECList, Task newTask, double currTime) {
        List<MEC> S1 = new ArrayList<>();
        List<MEC> S2 = new ArrayList<>();
        for (MEC mec : beforeMECList) {
            if (mec.canLocalDES(newTask, currTime)) {
                if (mec.findInsertIdx(newTask) == mec.getTaskList().size()) {
                    // 插入位置在末尾
                    S1.add(mec);
                } else {
                    // 插入位置在中间
                    S2.add(mec);
                }
            }
        }
//        logger.info("选取最优MEC时，S1 = " + S1.size() + " S2 = " + S2.size());


        if (S1.size() > 0) {    // EST First
            MEC earlyMEC = S1.get(0);    // 具有任务最早执行时间的MEC id
            double earlyTime = earlyMEC.getST(currTime);
            for (int j = 1; j < S1.size(); j++) {
                if (S1.get(j).getST(currTime) < earlyTime) {
                    earlyMEC = S1.get(j);
                    earlyTime = earlyMEC.getST(currTime);
                }
            }
//            logger.info("任务" + newTask.getId() + "插入位置为末端");
            return earlyMEC;
        } else if (S2.size() > 0) {  // 最短可用空闲间隔优先(SSFI)
            MEC minIntervalMEC = S2.get(0);
//            System.out.println(newTask);
//            System.out.println("id:"+ minIntervalMEC.getId()+ " c:" +minIntervalMEC.getC());
//            System.out.println(minIntervalMEC.getTaskList());
            double minInterval = S2.get(0).getSFI(newTask);
            for (int j = 1; j < S2.size(); j++) {
                if (S2.get(j).getSFI(newTask) < minInterval) {
                    minIntervalMEC = S2.get(j);
                    minInterval = minIntervalMEC.getSFI(newTask);
                }
            }
//            logger.info("任务" + newTask.getId() + "插入位置为中间");
            return minIntervalMEC;
        } else {
            return null;
        }
    }


    /**
     * 任务重组算法
     *
     * @param beforeMECList
     * @param newTask
     * @param currTime
     * @return
     */
    public static RegroupParam regroupTask(List<MEC> beforeMECList, Task newTask, double currTime) {
        List<MEC> S1 = new ArrayList<>();
        List<MEC> S2 = new ArrayList<>();
        for (MEC mec : beforeMECList) {
            if (mec.findInsertIdx(newTask) == mec.getTaskList().size()) {
                // 插入位置在末尾,由于当前任务不能移除，所以队长至少为2
                if (mec.getTaskList().size() >= 2) {
                    S1.add(mec);
                }
            } else {
                // 插入位置在中间
                S2.add(mec);
            }
        }
//        logger.info("任务重组时S1:" + S1.size() + "  S2:" + S2.size());
        RegroupParam res = null;
        if (S1.size() > 0) {
            res = regroupTaskS1(S1, newTask, currTime);
        }
        if (res == null) {
            if (S2.size() > 0)
                res = regroupTaskS2(S2, newTask, currTime);
        }
        return res;
    }


    /**
     * 任务重组方案S1, 插入位置为末端
     *
     * @param beforeMECList
     * @param newTask
     * @param currTime
     * @return
     */
    public static RegroupParam regroupTaskS1(List<MEC> beforeMECList, Task newTask, double currTime) {
//        logger.info("尝试任务重组S1中的MEC");
        List<MEC> tmpMECList = DeepCopy.deepCopy(beforeMECList);
        // 按照超额任务量从小到大排序
        tmpMECList.sort((m1, m2) ->
                (m1.getST(currTime) + newTask.getL() / m1.getC() - newTask.getDeadline()) * m1.getC() - (m2.getST(currTime) + newTask.getL() / m2.getC() - newTask.getDeadline()) * m2.getC() < 0 ? -1 :
                        ((newTask.getDeadline() - m1.getST(currTime)) * m1.getC() - (newTask.getDeadline() - m2.getST(currTime)) * m2.getC() == 0) ? 0 : 1);

        for (MEC mec : tmpMECList) {
//            logger.info("检查MEC-" + mec.getId() + ", 计算能力：" + mec.getC());
//            logger.info("MEC任务列表" + mec.getTaskList());

            // 其他MEC列表
            List<MEC> otherMECList = DeepCopy.deepCopy(tmpMECList);
            for (int i = otherMECList.size() - 1; i >= 0; i--) {
                if (otherMECList.get(i).getId() == mec.getId()) {
                    otherMECList.remove(i);
                    break;
                }
            }
            // 移动方案
            Map<Task, MEC> transfer = new HashMap<>();
            boolean isOK = false;

            Iterator<Task> iterator = mec.getTaskList().iterator();
            if (iterator.hasNext()) {
                iterator.next();
            }
            while (iterator.hasNext()) {
                double finishTime = mec.getST(currTime) + newTask.getL() / mec.getC();   // 任务原结束时间
                Task iteratorTask = iterator.next();
                if (iteratorTask.getL() < newTask.getL()) {
                    MEC chooseMEC = getBestMEC(otherMECList, iteratorTask, currTime);
                    if (chooseMEC != null) {
                        chooseMEC.acceptAndUpdateTime(iteratorTask, currTime);
                        transfer.put(iteratorTask, chooseMEC);
                        finishTime -= iteratorTask.getL() / mec.getC();
                        iterator.remove();
//                        logger.info("任务" + iteratorTask.getId() + "可迁移到MEC" + chooseMEC.getId());
                        if (finishTime <= newTask.getDeadline()) {
                            isOK = true;
                            break;
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (isOK) {
//                logger.info("S1替换成功，重组成功的MEC为" + mec.getId());
                return new RegroupParam(mec, transfer);
            }
        }
//        logger.info("S1替换失败");
        return null;
    }


    /**
     * 任务重组方案S2, 插入位置为中间
     *
     * @param beforeMECList
     * @param newTask
     * @param currTime
     * @return
     */
    public static RegroupParam regroupTaskS2(List<MEC> beforeMECList, Task newTask, double currTime) {
//        logger.info("尝试任务重组S2中的MEC");
        List<MEC> tmpMECList = DeepCopy.deepCopy(beforeMECList);
        tmpMECList.sort((m1, m2) ->
                m1.getSFI2(newTask) - m2.getSFI2(newTask) < 0 ? 1 : (m1.getSFI2(newTask) - m2.getSFI2(newTask) == 0) ? 0 : -1);

//        logger.info("S2中的MEC如下：");
//        for (MEC mec : tmpMECList) {
//            logger.info("MEC" + mec.getId() + " 计算能力: " + mec.getC() + ", SFI: " + mec.getSFI2(newTask));
//        }


        for (MEC mec : tmpMECList) {
//            logger.info("尝试重组MEC" + mec.getId());
            // 其他MEC列表
            List<MEC> otherMECList = DeepCopy.deepCopy(tmpMECList);
            for (int i = otherMECList.size() - 1; i >= 0; i--) {
                if (otherMECList.get(i).getId() == mec.getId()) {
                    otherMECList.remove(i);
                    break;
                }
            }
            // 移动方案
            Map<Task, MEC> transfer = new HashMap<>();
            boolean isOK = false;

            Iterator<Task> iterator = mec.getTaskList().iterator();
            if (iterator.hasNext()) {
                iterator.next();
            }
            while (iterator.hasNext()) {
                Task iteratorTask = iterator.next();
                if (iteratorTask.getL() < newTask.getL()) {
                    MEC chooseMEC = getBestMEC(otherMECList, iteratorTask, currTime);
                    if (chooseMEC != null) {
                        chooseMEC.acceptAndUpdateTime(iteratorTask, currTime);
                        transfer.put(iteratorTask, chooseMEC);
                        iterator.remove();
                        mec.updateTimeAfterRemove();    // 删除任务后更新剩下任务的开始时间
//                        if (mec.getSFI2(newTask) > newTask.getL()) {
//                            isOK = true;
//                            break;
//                        }
//                        logger.info("任务" + iteratorTask.getId() + "可迁移到MEC" + chooseMEC.getId());
                        if (mec.canLocalDES(newTask, currTime)) {
                            isOK = true;
                            break;
                        }
                    } else {
//                        logger.info("任务" + iteratorTask.getId() + "找不到可以迁移的MEC");
                        continue;
                    }
                }
            }
            if (isOK) {
//                logger.info("S2替换成功，重组成功的MEC为" + mec.getId());
                return new RegroupParam(mec, transfer);
            }
//            logger.info("重组MEC" + mec.getId() + "失败");
        }
//        logger.info("S2替换失败");
        return null;
    }


    /**
     * DES直接执行策略：找到最佳的MEC
     *
     * @param afterMECList
     * @param newTask
     * @param currTime
     * @return
     */
    public static MEC getBestTmpPreempt(List<MEC> afterMECList, Task newTask, double currTime) {
        List<MEC> S1 = new ArrayList<>();
        for (MEC mec : afterMECList) {
            if (mec.canPreempt(newTask, currTime)) {
                if (mec.findInsertIdx(newTask) == mec.getTaskList().size()) {
                    // 插入位置在末尾
                    S1.add(mec);
                }
            }
        }
        if (S1.size() > 0) {    // EST First
            MEC earlyMEC = S1.get(0);    // 具有任务最早执行时间的MEC id
            double earlyTime = earlyMEC.getST(currTime);
            for (int j = 1; j < S1.size(); j++) {
                if (S1.get(j).getST(currTime) < earlyTime) {
                    earlyMEC = S1.get(j);
                    earlyTime = earlyMEC.getST(currTime);
                }
            }
            return earlyMEC;
        } else {
            return null;
        }
    }


}
