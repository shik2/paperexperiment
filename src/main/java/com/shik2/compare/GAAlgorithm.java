package com.shik2.compare;

import com.shik2.earliest.Task;
import com.shik2.ga.MEC;

/**
 * @author shik2
 * @date 2020/12/28
 * <p>
 * Description:
 **/
public class GAAlgorithm {
    public static boolean acceptTask(MEC chooseMEC, Task task, double currTime) {
        int tasksSize = chooseMEC.getTaskList().size();
        double st = 0;
        if (tasksSize > 0) {
            Task lastTask = chooseMEC.getTaskList().get((tasksSize - 1));
            st = lastTask.getStartTime() + lastTask.getL() / chooseMEC.getC();
        } else {
            st = currTime;
        }
        if (st + task.getL() / chooseMEC.getC() < task.getDeadline()) {
            task.setStartTime(st);
            chooseMEC.getTaskList().add(task);
            return true;
        } else {
            return false;
        }
    }


    public static boolean acceptTask2(MEC chooseMEC, Task task, double currTime) {
        int tasksSize = chooseMEC.getTaskList().size();
        double st = 0;
        if (tasksSize > 0) {
            Task lastTask = chooseMEC.getTaskList().get((tasksSize - 1));
            st = lastTask.getStartTime() + lastTask.getL() / chooseMEC.getC();
        } else {
            st = currTime;
        }
        task.setStartTime(st);
        chooseMEC.getTaskList().add(task);
        return true;
    }

}
