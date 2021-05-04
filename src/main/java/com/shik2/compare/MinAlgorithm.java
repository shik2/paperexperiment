package com.shik2.compare;

import com.shik2.earliest.Task;
import com.shik2.ga.MEC;
import com.shik2.utils.DataProcess;

/**
 * @author shik2
 * @date 2020/12/29
 * <p>
 * Description:
 **/
public class MinAlgorithm {
    public static boolean canAcceptTask(MEC currMEC, Task task, double currTime) {
        int tasksSize = currMEC.getTaskList().size();
        double st = 0;
        if (tasksSize > 0) {
            Task lastTask = currMEC.getTaskList().get((tasksSize - 1));
            st = lastTask.getStartTime() + lastTask.getL() / currMEC.getC();
        } else {
            st = currTime;
        }
        if (st + task.getL() / currMEC.getC() < task.getDeadline()) {
            return true;
        } else {
            return false;
        }
    }

    public static double calFinishTime(MEC currMEC, Task task, double currTime) {
        double st = 0;
        if (currMEC.getTaskList().size() > 0) {
            Task lastTask = currMEC.getTaskList().get((currMEC.getTaskList().size() - 1));
            st = lastTask.getStartTime() + lastTask.getL() / currMEC.getC();
        } else {
            st = currTime;
        }
        return st + task.getL() / currMEC.getC();
    }

    public static double calStartTime(MEC currMEC, double currTime) {
        double st = 0;
        if (currMEC.getTaskList().size() > 0) {
            Task lastTask = currMEC.getTaskList().get((currMEC.getTaskList().size() - 1));
            st = lastTask.getStartTime() + lastTask.getL() / currMEC.getC();
        } else {
            st = currTime;
        }
        return DataProcess.format(st, 2);
    }
}
