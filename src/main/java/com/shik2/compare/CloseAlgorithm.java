package com.shik2.compare;

import com.shik2.earliest.Task;
import com.shik2.ga.MEC;

/**
 * @author shik2
 * @date 2020/12/27
 * <p>
 * Description:
 **/
public class CloseAlgorithm {

    public static boolean acceptTask(MEC closestMEC, Task task, double currTime) {
        int tasksSize = closestMEC.getTaskList().size();
        double st = 0;
        if (tasksSize > 0) {
            Task lastTask = closestMEC.getTaskList().get((tasksSize - 1));
            st = lastTask.getStartTime() + lastTask.getL() / closestMEC.getC();
        } else {
            st = currTime;
        }
        if (st + task.getL() / closestMEC.getC() < task.getDeadline()) {
            task.setStartTime(st);
            closestMEC.getTaskList().add(task);
            return true;
        } else {
            return false;
        }
    }
}
