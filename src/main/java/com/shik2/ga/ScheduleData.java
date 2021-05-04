package com.shik2.ga;

import com.shik2.earliest.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shik2
 * @date 2020/12/17
 * <p>
 * Description:
 **/
public class ScheduleData {
    // 用于计算适应度的MEC列表
    public static List<MEC> mecList = new ArrayList<>();
    // 用于计算适应度的Task列表
    public static List<Task> taskList = new ArrayList<>();

    public static List<MEC> getMecList() {
        return mecList;
    }

    public static void setMecList(List<MEC> mecList) {
        ScheduleData.mecList = mecList;
    }

    public static List<Task> getTaskList() {
        return taskList;
    }

    public static void setTaskList(List<Task> taskList) {
        ScheduleData.taskList = taskList;
    }
}
