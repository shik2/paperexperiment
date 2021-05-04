package com.shik2.ga;

/**
 * @author: shik2
 * @date: 2020/12/16 0016 16:07
 * <p>
 * Description: 种群
 */
public class Population {
    Individual[] individuals;
    // 交叉概率
    private double uniformRate;
    // 突变概率
    private double mutationRate;

    /**
     * 创建一个种群
     *
     * @param populationSize 种群初始规模
     * @param initialise     是否初始化
     */
    public Population(int populationSize, boolean initialise, double currTime, double uniformRate, double mutationRate) {
        individuals = new Individual[populationSize];
        // 初始化种群
        if (initialise) {
            for (int i = 0; i < size(); i++) {
                Individual newIndividual = new Individual();
                newIndividual.generateIndividual();     // 创建基因个体
                newIndividual.updateFitness(currTime);  // 更新个体适应度
                saveIndividual(i, newIndividual);
            }
        }
        this.uniformRate = uniformRate;
        this.mutationRate = mutationRate;
    }


    /**
     * 创建一个合并种群
     *
     * @param populationSize 种群初始规模
     */
    public Population(int populationSize) {
        individuals = new Individual[populationSize];
    }


    /**
     * 更新种群个体适应度
     *
     * @param currTime
     */
    public void updateFitness(double currTime) {
        for (int i = 0; i < size(); i++) {
            individuals[i].updateFitness(currTime);
        }
    }

    public Individual getIndividual(int index) {
        return individuals[index];
    }

    /**
     * 获取种群中最优个体
     *
     * @return
     */
    public Individual getFittest() {
        Individual fittest = individuals[0];
        double maxFitness = fittest.getFitness();
        for (int i = 1; i < size(); i++) {
            if (maxFitness <= individuals[i].getFitness()) {
                fittest = individuals[i];
                maxFitness = individuals[i].getFitness();
            }
        }
        return fittest;
    }

    /**
     * 获取种群中最差个体
     *
     * @return
     */
    public Individual getWorstIdv() {
        Individual worstIdv = individuals[0];
        double minFitness = worstIdv.getFitness();
        for (int i = 1; i < size(); i++) {
            if (minFitness >= individuals[i].getFitness()) {
                worstIdv = individuals[i];
                minFitness = individuals[i].getFitness();
            }
        }
        return worstIdv;
    }


    public int size() {
        return individuals.length;
    }

    public void saveIndividual(int index, Individual indiv) {
        individuals[index] = indiv;
    }


    public double getUniformRate() {
        return uniformRate;
    }


    public double getMutationRate() {
        return mutationRate;
    }


    public Individual[] getIndividuals() {
        return individuals;
    }
}