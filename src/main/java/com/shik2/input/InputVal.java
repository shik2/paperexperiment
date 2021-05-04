package com.shik2.input;

import com.shik2.earliest.Task;

import java.io.Serializable;
import java.util.List;


public class InputVal implements Serializable {
    private int lamda;
    private List<Task> taskList;
    private static final long serialVersionUID = 3l;

    public InputVal(int lamda, List<Task> taskList) {
        this.lamda = lamda;
        this.taskList = taskList;
    }

    public int getLamda() {
        return lamda;
    }

    public void setLamda(int lamda) {
        this.lamda = lamda;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public String toString() {
        return "InputVal{" +
                "lamda=" + lamda +
                ", taskSize=" + taskList.size() +
                '}';
    }
}
