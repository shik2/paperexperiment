package com.shik2.earliest;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author: shik2
 * @date: 2020/12/19 0019 11:09
 *
 * Description: 任务类
 *
 */
public class Task implements Serializable {
    private Integer id;
    private Double l;   // 负载计算量
    private Integer urgency;    // 紧急程度
    private Double oDeadline;   // 一般情况下的截止时间
    private Double deadline;    // 最终截止时间
    private Double dlClosest;  // 最近调度的截止时间
    private Double startTime;  // 开始时间
    private double arriveTime; // 任务到达时间
    private Integer closestMEC; // 任务卸载最近的MEC



    private static final long serialVersionUID = 1l;

    public Task(Double l, Integer urgency, Double oDeadline) {
        this.l = l;
        this.urgency = urgency;
        this.oDeadline = oDeadline;
    }

    public Double getL() {
        return l;
    }

    public void setL(Double l) {
        this.l = l;
    }

    public Integer getUrgency() {
        return urgency;
    }

    public void setUrgency(Integer urgency) {
        this.urgency = urgency;
    }

    public Double getoDeadline() {
        return oDeadline;
    }

    public void setoDeadline(Double oDeadline) {
        this.oDeadline = oDeadline;
    }

    public Double getDeadline() {
        return deadline;
    }

    public void setDeadline(Double deadline) {
        this.deadline = deadline;
    }

    public Double getStartTime() {
        return startTime;
    }

    public void setStartTime(Double startTime) {
        this.startTime = startTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(double arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Integer getClosestMEC() {
        return closestMEC;
    }

    public void setClosestMEC(Integer closestMEC) {
        this.closestMEC = closestMEC;
    }

    public Double getDlClosest() {
        return dlClosest;
    }

    public void setDlClosest(Double dlClosest) {
        this.dlClosest = dlClosest;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", l=" + l +
                ", urgency=" + urgency +
                ", oDeadline=" + oDeadline +
                ", deadline=" + deadline +
                ", startTime=" + startTime +
                ", arriveTime=" + arriveTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
