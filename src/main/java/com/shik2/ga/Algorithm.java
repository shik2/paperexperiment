package com.shik2.ga;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: shik2
 * @date: 2020/12/16 0016 16:10
 * <p>
 * Description: 遗传算法实现类
 */
public class Algorithm {
    //    // 交叉概率
//    private static final double uniformRate = 0.6;
//    // 突变概率
//    private static final double mutationRate = 0.05;
    // 淘汰数组的大小
    private static final int tournamentSize = 2;
    // 精英主义
    private static final boolean elitism = true;

    /**
     * 进化一个种群
     *
     * @param pop 原始种群
     * @return 进化后的种群
     */
    public static Population evolvePopulation(Population pop, double currTime) {
        // 存放新一代的种群
        Population newPopulation = new Population(pop.size(), false, currTime, pop.getUniformRate(), pop.getMutationRate());

        // 精英主义策略，存放适应度最高的个体
        if (elitism) {
            newPopulation.saveIndividual(0, pop.getFittest());
//            System.out.println("放入最优的染色体：" + pop.getFittest());
        }

        int elitismOffset;
        if (elitism) {
            elitismOffset = 1;
        } else {
            elitismOffset = 0;
        }

        //选择，交叉，变异
        for (int i = elitismOffset; i < pop.size(); i++) {
            // 随机选择两个优秀的个体
            Individual indiv1 = tournamentSelection(pop, currTime);
            Individual indiv2 = tournamentSelection(pop, currTime);
            // 进行交叉
            Individual newIndiv = crossover(indiv1, indiv2,pop.getUniformRate());
            newPopulation.saveIndividual(i, newIndiv);
        }

        // 种群突变
        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i),pop.getMutationRate());
        }

        // 更新当前种群个体的适应度
        newPopulation.updateFitness(currTime);

        return newPopulation;
    }

    /**
     * 种群间迁移，source中最好个体和target最差个体交换
     *
     * @param sourcePop
     * @param targetPop
     */
    public static void migratePopulation(Population sourcePop, Population targetPop) {
        Individual bestSIdv = sourcePop.getFittest();
        Individual worstTIdv = targetPop.getWorstIdv();
        Individual.swap(bestSIdv, worstTIdv);
    }


    /**
     * 随机选择一个较优秀的个体。用于进行交叉（锦标赛）
     *
     * @param pop 种群
     * @return
     */
    private static Individual tournamentSelection(Population pop, double currTime) {
        Population tournamentPop = new Population(tournamentSize, false, currTime, pop.getUniformRate(), pop.getMutationRate());
        // 随机选择 tournamentSize 个放入 tournamentPop 中
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.size());
            tournamentPop.saveIndividual(i, pop.getIndividual(randomId));
        }
        // 找到淘汰数组中最优秀的
        Individual fittest = tournamentPop.getFittest();
        return fittest;
    }


    /**
     * 两个个体交叉产生下一代
     *
     * @param indiv1 父亲
     * @param indiv2 母亲
     * @return 后代
     */
    private static Individual crossover(Individual indiv1, Individual indiv2,double uniformRate) {
        Individual newSol = new Individual();
        // 随机的从两个个体中选择
        for (int i = 0; i < indiv1.size(); i++) {
            if (Math.random() <= uniformRate) {
                newSol.setGene(i, indiv1.getGene(i));
            } else {
                newSol.setGene(i, indiv2.getGene(i));
            }
        }
        return newSol;
    }

    /**
     * 突变个体。突变的概率为 mutationRate
     *
     * @param indiv 待突变的个体
     */
    private static void mutate(Individual indiv,double mutationRate) {
        for (int i = 0; i < indiv.size(); i++) {
            if (Math.random() <= mutationRate) {
                int gene = (int) Math.round(Math.random()) * (ScheduleData.mecList.size() - 1);
                indiv.setGene(i, gene);
            }
        }
    }


}