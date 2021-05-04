package com.shik2.ga;

import com.shik2.Statistics;
import com.shik2.earliest.Task;
import com.shik2.utils.DataProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author shik2
 * @date 2020/12/16
 * <p>
 * Description:
 **/
public class MEC implements Serializable {
    private int id;
    private Double c;
    private ArrayList<Task> taskList;

    private static final long serialVersionUID = 2l;

    private static Logger logger = LoggerFactory.getLogger(MEC.class);

    public MEC(int id, Double c) {
        this.id = id;
        this.c = c;
        this.taskList = new ArrayList<>();
    }

    /**
     * 新任务入队
     *
     * @param task
     */
    public void addTask(Task task) {
        taskList.add(task);
    }


    /**
     * 计算任务的排队时延
     *
     * @param currTime
     * @return
     */
    public double calWaitTime(double currTime) {
        double waitTime = 0;
        if (taskList.size() == 0) {
            return waitTime;
        }
        // 计算正在执行任务的剩余时间 T_remain
        if (taskList.get(0).getStartTime() <= currTime && (taskList.get(0).getStartTime() + taskList.get(0).getL() / c) > currTime) {
            waitTime += taskList.get(0).getStartTime() + taskList.get(0).getL() / c - currTime;
        }
        // 队列中的任务 T_exe
        for (int i = 1; i < taskList.size(); i++) {
            waitTime += taskList.get(i).getL() / c;
        }
        return waitTime;
    }

    /**
     * 计算任务的运行时延
     *
     * @param newTask
     * @return
     */
    public double calRunTime(Task newTask) {
        return newTask.getL() / c;
    }


    /**
     * GA，MEC接受新任务并更新任务列表中任务的时间
     *
     * @param newTask
     * @return
     */
    public void gaAcceptAndUpdateTime(Task newTask, double currTime) {
        double st = taskList.size() > 0 ? taskList.get(taskList.size() - 1).getStartTime() + taskList.get(taskList.size() - 1).getL() / c : currTime;
        newTask.setStartTime(DataProcess.format(st, 2));
        addTask(newTask);
    }


    /**
     * 移除在当前时间前已经完成的任务
     *
     * @param currTime
     */
    public void removeCompletedTasks(double currTime, Statistics sts) {
        while (taskList.size() > 0 && taskList.get(0).getStartTime() + taskList.get(0).getL() / c < currTime) {
//            logger.info("MEC" + id + " c=" + c + " 移除任务" + taskList.get(0));
            // 统计普通任务的时延
            if (taskList.get(0).getUrgency() == 3) {
                sts.totalNormalDelay += (taskList.get(0).getStartTime() + taskList.get(0).getL() / c - taskList.get(0).getArriveTime());
                sts.normalTask++;
            }
            taskList.remove(0);
        }
    }





    /**
     * 新任务入队
     *
     * @param task
     */
    public void add(Task task, int idx) {
        taskList.add(idx, task);
    }

    /**
     * 删除指定任务
     *
     * @param task
     */
    public void remove(Task task) {
        Iterator<Task> iterator = taskList.iterator();
        while (iterator.hasNext()) {
            Task t = iterator.next();
            if (task.equals(t)) {
                iterator.remove();
            }
        }
    }


    /**
     * 按任务最终截止时间排序
     */
    public synchronized void sort() {
        if (taskList.size() > 0) {
            taskList.sort((t1, t2) -> t1.getDeadline() - t2.getDeadline() < 0 ? -1 : (t1.getDeadline() - t2.getDeadline() == 0) ? 0 : 1);
        }
    }


    /**
     * 找到新任务的插入位置
     *
     * @param task
     * @return
     */
    public synchronized int findInsertIdx(Task task) {
        if (taskList.size() == 0) {
            return 0;
        } else {
            int idx = 1;
            while (idx < taskList.size() && taskList.get(idx).getDeadline() <= task.getDeadline()) {
                idx++;
            }
            return idx;
        }
    }

    /**
     * 移除队头元素
     */
    public void removeFirst() {
        taskList.remove(0);
    }


    /**
     * DES算法中的当前MEC接受新任务并更新任务列表中任务的时间
     *
     * @param newTask
     * @return
     */
    public void acceptAndUpdateTime(Task newTask, double currTime) {
        int insertIdx = findInsertIdx(newTask);
        double TC_W;
        if (taskList.size() > 0) {
            Task task_now = taskList.get(0);
            TC_W = task_now.getStartTime() + task_now.getL() / c; // 当前正在运行的任务+排在任务Ti前的任务执行时间总和
        } else {
            TC_W = currTime;
        }
        for (int j = 1; j < insertIdx; j++) {   // 配置队列前部分对应的开始时间
            taskList.get(j).setStartTime(DataProcess.format(TC_W, 2));
            TC_W += taskList.get(j).getL() / c;
        }
        newTask.setStartTime(DataProcess.format(TC_W, 2));
        // 同时满足两个条件后
        for (int j = insertIdx; j < taskList.size(); j++) {     // 配置新任务插入队列后的开始时间
            taskList.get(j).setStartTime(DataProcess.format(taskList.get(j).getStartTime() + newTask.getL() / c, 2));
        }
        add(newTask, insertIdx);
    }


    /**
     * DES算法中的判断是否可以本地直接运行
     *
     * @param newTask
     * @return
     */
    public boolean canLocalDES(Task newTask, double currTime) {
        int insertIdx = findInsertIdx(newTask);
        int flag1, flag2 = 1;
        double TC_W;
        if (taskList.size() > 0) {
            Task task_now = taskList.get(0);
            TC_W = task_now.getStartTime() + task_now.getL() / c; // 当前正在运行的任务+排在任务Ti前的任务执行时间总和
        } else {
            TC_W = currTime;
        }
        for (int j = 1; j < insertIdx; j++) {   // 配置队列前部分对应的开始时间
            TC_W += taskList.get(j).getL() / c;
        }
        double TC_OW = TC_W;
        for (int j = insertIdx; j < taskList.size(); j++) {     // 配置队列后部分对应的原开始时间
            TC_OW += taskList.get(j).getL() / c;
            if (TC_OW + newTask.getL() / c > taskList.get(j).getDeadline()) {
                flag2 = 0;  // 一旦出现超时任务，该新任务将无法添加入队
            }
        }
        // 判断是否满足DES的第一个条件
        TC_W += newTask.getL() / c;
        flag1 = TC_W < newTask.getDeadline() ? 1 : 0;
        // 同时满足两个条件后
        if (flag1 == 1 && flag2 == 1) {
            return true;
        } else {
            return false;
        }
    }

    //  canLocalDES的打印测试方法
    public boolean canLocalDES2(Task newTask, double currTime) {
        int insertIdx = findInsertIdx(newTask);
        int flag1, flag2 = 1;
        double TC_W;
        if (taskList.size() > 0) {
            Task task_now = taskList.get(0);
            TC_W = task_now.getStartTime() + task_now.getL() / c; // 当前正在运行的任务+排在任务Ti前的任务执行时间总和
        } else {
            TC_W = currTime;
        }
        for (int j = 1; j < insertIdx; j++) {   // 配置队列前部分对应的开始时间
            TC_W += taskList.get(j).getL() / c;
        }
        double TC_OW = TC_W;
        for (int j = insertIdx; j < taskList.size(); j++) {     // 配置队列后部分对应的原开始时间
            TC_OW += taskList.get(j).getL() / c;
            if (TC_OW + newTask.getL() / c > taskList.get(j).getDeadline()) {
                flag2 = 0;  // 一旦出现超时任务，该新任务将无法添加入队
            }
        }
        // 判断是否满足DES的第一个条件
        TC_W += newTask.getL() / c;
        logger.info("TC_W: " + TC_W);
        flag1 = TC_W < newTask.getDeadline() ? 1 : 0;
        logger.info(flag1 + " " + flag2);
        // 同时满足两个条件后
        if (flag1 == 1 && flag2 == 1) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * DES算法第二部分，其他ECS直接执行
     * 计算S1中各ECS的开始时间
     *
     * @return
     */
    public double getST(double currTime) {
        if (taskList.size() == 0) {
            return currTime;
        } else {
            return taskList.get(taskList.size() - 1).getStartTime() + taskList.get(taskList.size() - 1).getL() / c;
        }
    }

    /**
     * 计算S2中各ECS的空闲间隔
     * 改进思路：可以把时间间隔等价为任务量间隔，用时间*计算能力表示
     *
     * @param newTask
     * @return
     */
    public double getSFI(Task newTask) {
        int insertIdx = findInsertIdx(newTask);
        double startTime = taskList.get(insertIdx).getStartTime();
        double endTime = taskList.get(taskList.size() - 1).getDeadline() - taskList.get(taskList.size() - 1).getL() / c;
        for (int i = taskList.size() - 2; i >= insertIdx; i--) {
            if (endTime > taskList.get(i).getDeadline()) {
                endTime = taskList.get(i).getDeadline() - taskList.get(i).getL() / c;
            } else {
                endTime -= taskList.get(i).getL() / c;
            }
        }
        return (endTime - startTime) * c;
    }

    /**
     * 任务替换阶段S2 SFI计算，区别在于可提供任务量要在newTask的截止时间前
     * endTime的计算方式有区别
     *
     * @param newTask
     * @return
     */
    public double getSFI2(Task newTask) {
        int insertIdx = findInsertIdx(newTask);
        double startTime = taskList.get(insertIdx).getStartTime();
        double endTime = taskList.get(taskList.size() - 1).getDeadline() - taskList.get(taskList.size() - 1).getL() / c;
        for (int i = taskList.size() - 2; i >= insertIdx; i--) {
            if (endTime > taskList.get(i).getDeadline()) {
                endTime = taskList.get(i).getDeadline() - taskList.get(i).getL() / c;
            } else {
                endTime -= taskList.get(i).getL() / c;
            }
        }
        // 可用间隔不能超过deadline
        endTime = endTime > newTask.getDeadline() ? newTask.getDeadline() : endTime;
//        return DataProcess.format((endTime - startTime) * c, 2);
        return (endTime - startTime) * c;
    }


    public void updateTimeAfterRemove() {
        if (taskList.size() > 1) {
            double tw = taskList.get(0).getStartTime();
            for (int i = 1; i < taskList.size(); i++) {
                tw += taskList.get(i - 1).getL() / c;
                taskList.get(i).setStartTime(tw);
            }
        }
    }

    /**
     * 判断一个任务是否可以在当前MEC抢占执行
     *
     * @param newTask
     * @param currTime
     * @return
     */
    public boolean canPreempt(Task newTask, double currTime) {
        if (taskList.size() > 0) {
            double st = taskList.get(taskList.size() - 1).getStartTime() + taskList.get(0).getL() / c;
            if (st + newTask.getL() / c <= newTask.getDeadline()) {
                return true;
            }
        }
        return false;
    }


    public Task getTaskById(Integer id) {
        for (Task task : this.taskList) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getC() {
        return c;
    }

    public void setC(Double c) {
        this.c = c;
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public String toString() {
        return "MEC{" +
                "id=" + id +
                ", c=" + c +
                ", taskList=" + taskList +
                '}';
    }
}
