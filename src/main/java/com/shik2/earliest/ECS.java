package com.shik2.earliest;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author shik2
 * @date 2020/11/23
 **/
public class ECS {
    private int id;
    private Double c;
    private ArrayList<Task> taskList;
    private double currTime;
    private int finishTask = 0;

    /**
     * 按最终截止时间排序
     */
    public synchronized void sort() {
        if(taskList.size()>0){
            taskList.sort((t1, t2) -> t1.getDeadline() - t2.getDeadline() < 0 ? -1 : (t1.getDeadline() - t2.getDeadline() == 0) ? 0 : 1);
        }
    }

    /**
     * 新任务入队
     *
     * @param task
     */
    public void add(Task task) {
        taskList.add(task);
        sort();
    }

    /**
     * 找到新任务的插入位置
     *
     * @param task
     * @return
     */
    public synchronized int findInsertIdx(Task task) {
        int idx = 0;
        if(taskList.size() > 0){
            while (idx < taskList.size() && taskList.get(idx).getDeadline() <= task.getDeadline()) {
                idx++;
            }
        }
        return idx;
    }

    /**
     * 移除队头元素
     */
    public void removeFirst() {
        taskList.remove(0);
    }

    /**
     * 找到比target时间计算时间短的任务
     *
     * @param target
     */
    public Task removeLessTask(Task target) {
        Iterator<Task> iter = taskList.iterator();
        while (iter.hasNext()) {
            Task item = iter.next();
            if (item.getL() < target.getL()) {
                return item;
            }
        }
        return null;
    }


    /**
     * DES算法中的本地直接运行部分
     *
     * @param newTask
     * @return
     */
    public boolean localDES(Task newTask) {
        int insertIdx = findInsertIdx(newTask);
        int flag1, flag2 = 1;
        double TC_W;
        if(taskList.size() > 0){
            Task task_now = taskList.get(0);
            TC_W = task_now.getStartTime() + task_now.getL() / c; // 当前正在运行的任务+排在任务Ti前的任务执行时间总和
        }else {
            TC_W = currTime;
        }
        for (int j = 1; j < insertIdx; j++) {   // 配置队列前部分对应的开始时间
            taskList.get(j).setStartTime(TC_W);
            TC_W += taskList.get(j).getL() / c;
        }
        double TC_OW = TC_W;
        for (int j = insertIdx; j < taskList.size(); j++) {     // 配置队列后部分对应的原开始时间
            taskList.get(j).setStartTime(TC_OW);
            TC_OW += taskList.get(j).getL() / c;
            if (TC_OW + newTask.getL() / c > taskList.get(j).getDeadline()) {
                flag2 = 0;  // 一旦出现超时任务，该新任务将无法添加入队
            }
        }

        newTask.setStartTime(TC_W);
        // 判断是否满足DES的第一个条件
        TC_W += newTask.getL() / c;
        flag1 = TC_W < newTask.getDeadline() ? 1 : 0;
        // 同时满足两个条件后
        if (flag1 == 1 && flag2 == 1) {
            for (int j = insertIdx; j < taskList.size(); j++) {     // 配置新任务插入队列后的开始时间
                taskList.get(j).setStartTime(taskList.get(j).getStartTime() + newTask.getL() / c);
            }
            add(newTask);
            finishTask++;
            return true;
        } else {
            return false;
        }
    }


    /**
     * DES算法中的判断是否可以本地直接运行
     *
     * @param newTask
     * @return
     */
    public boolean canLocalDES(Task newTask) {
        int insertIdx = findInsertIdx(newTask);
        int flag1, flag2 = 1;
        double TC_W;
        if(taskList.size() > 0){
            Task task_now = taskList.get(0);
            TC_W = task_now.getStartTime() + task_now.getL() / c; // 当前正在运行的任务+排在任务Ti前的任务执行时间总和
        }else {
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


    /**
     * DES算法第二部分，其他ECS直接执行
     * 计算S1中各ECS的开始时间
     *
     * @return
     */
    public double getST() {
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
        for (int i = taskList.size() - 1; i > insertIdx; i--) {
            if (endTime > taskList.get(i).getDeadline()) {
                endTime = taskList.get(i).getDeadline() - taskList.get(i).getL() / c;
            } else {
                endTime -= taskList.get(i).getL() / c;
            }
        }
        return (endTime - startTime) * c;
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

    public double getCurrTime() {
        return currTime;
    }

    public void setCurrTime(double currTime) {
        this.currTime = currTime;
    }

    public int getId() {
        return id;
    }

    public int getFinishTask() {
        return finishTask;
    }

    public ECS(int id, Double c, double currTime) {
        this.id = id;
        this.c = c;
        this.currTime = currTime;
        this.taskList = new ArrayList<>();
    }
}
