package com.shik2.ga;

import com.shik2.utils.DeepCopy;

import java.io.*;

/**
 * @author: shik2
 * @date: 2020/12/16 0016 15:49
 * <p>
 * Description: 个体
 */
public class Individual implements Serializable {
    // 默认10个任务做一次调度
    static int defaultGeneLength = 3;
    // 基因序列
    private int[] genes = new int[defaultGeneLength];
    // 个体的适应度
    private double fitness = 0;

    // 创建一个随机的基因个体
    public void generateIndividual() {
        // 每个任务基因位从MEC中随机挑选一台
        for (int i = 0; i < size(); i++) {
            int gene = (int) Math.round(Math.random() * (ScheduleData.getMecList().size() - 1));
            genes[i] = gene;
        }
    }

    // 更新个体适应度
    public void updateFitness(double currTime) {
        fitness = FitnessCalc.getFitness(this, currTime);
    }

    // 交换两个个体
    public static void swap(Individual a, Individual b) {
        Individual copyA = DeepCopy.deepCopy(a);
        b.fitness = copyA.fitness;
        b.genes = copyA.getGenes();
    }


    public static void setDefaultGeneLength(int length) {
        defaultGeneLength = length;
    }

    // setter and getter
    public int getGene(int index) {
        return genes[index];
    }

    public void setGene(int index, int value) {
        genes[index] = value;
        fitness = 0;
    }

    public static int getDefaultGeneLength() {
        return defaultGeneLength;
    }

    public int[] getGenes() {
        return genes;
    }

    public void setGenes(int[] genes) {
        this.genes = genes;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int size() {
        return genes.length;
    }

    public double getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            sb.append(" " + getGene(i));
        }
        return sb.toString();
    }

}