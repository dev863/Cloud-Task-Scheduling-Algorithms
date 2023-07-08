package ESFO;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import ESFO.FitnessFunction;
import utils.Constants;

import utils.GenerateMatrices;

import java.text.DecimalFormat;
import java.util.*;


public class ESFO {

    // Parameters of the algorithm
    private int n; // Number of tasks
    private int m; // Number of processors
    private int k; // Number of sunflowers
    private double c; // Convergence factor
    private int maxIterations; // Maximum number of iterations
    private double[][] processingTimes; // Processing times for each task and processor
    private int[][] schedules; // Schedule of tasks on each processor
    private double[] fitnessValues; // Fitness values for each sunflower
    private double[] bestFitnessValues=new double[Constants.NO_OF_TASKS]; // Best fitness values for each sunflower
    private int[][] bestSchedules; // Best schedules for each sunflower
    private Random random; // Random number generator
    //private static FitnessFunction ff = new FitnessFunction();
    public ESFO(int n, int m, int k, double c, int maxIterations, double[][] processingTimes) {
        this.n = n;
        this.m = m;
        this.k = k;
        this.c = c;
        this.maxIterations = maxIterations;
        this.processingTimes = processingTimes;
        this.schedules = new int[m][n];
        this.fitnessValues = new double[k];
       // this.bestFitnessValues = new double[k];
        this.bestSchedules = new int[k][m * n];
        this.random = new Random();
    }
    private void initialisefit()
    {
    	for(int i=0;i<n;i++)
    		bestFitnessValues[i]=Math.random()*10000;
    }
    
    // Initialize the schedules randomly
    private void initializeSchedules() {
        for (int i = 0; i < 30; i++) {
            List<Integer> tasks = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                tasks.add(j);
            }
            Collections.shuffle(tasks, random);
            for (int j = 0; j < n; j++) {
                schedules[i][j] = tasks.get(j);
            }
        }
    }

    // Evaluate the fitness value of each sunflower
    private void evaluateFitness() {
        for (int i = 0; i < 30; i++) {
            int[] schedule =schedules[i];
            
            double fitness = 0.0;
            for (int j = 0; j < n; j++) {
                for (int l = 0; l < m; l++) {
                    int task = schedule[j];
                    fitness += processingTimes[task][l];
                    
                }
                
            }
            fitnessValues[i] = fitness;
            
            if (fitness < bestFitnessValues[i]) {
                bestFitnessValues[i] = fitness;
                
            }
            System.out.println(bestFitnessValues[i]+"iteration:"+i);
        }
    }
    // Update the positions of the sunflowers
    private void updatePositions() {
    	for (int i = 0; i < k; i++) {
            if (random.nextDouble() < Constants.PP) { 
                int j = random.nextInt(k);
                int[] s1 = bestSchedules[i];
                int[] s2 = bestSchedules[j];
                int[] offspring = new int[m * n];
                int pivot = random.nextInt(m * n);
                for (int l = 0; l < m * n; l++) {
                    if (l < pivot) 
                        offspring[l] = s1[l];
                    else 
                        offspring[l] = s2[l];
                }
                // Replace worst sunflower with offspring
                int worstIndex = getWorstIndex();
                if (fitnessValues[worstIndex] < FitnessFunction.calculateFitness(offspring,processingTimes, m, n)) {
                    System.arraycopy(offspring, 0, bestSchedules[worstIndex], 0, m * n);
                    fitnessValues[worstIndex] = FitnessFunction.calculateFitness(offspring,processingTimes, m, n);
                }
            }
        }
    }
    private int getWorstIndex() {
        int worstIndex = 0;
        double worstFitness = fitnessValues[0];
        for (int i = 1; i < k; i++) {
            if (fitnessValues[i] > worstFitness) {
                worstFitness = fitnessValues[i];
                worstIndex = i;
            }
        }
        return worstIndex;
    }

        // Run the ESO algorithm
        public double[] run() {
        	initialisefit();
            initializeSchedules();
            evaluateFitness();
            System.arraycopy(bestFitnessValues, 0, fitnessValues, 0, k);
            for (int i = 0; i < 40; i++) {
                updatePositions();
                evaluateFitness();
                for (int j = 0; j < 20; j++) {
                    if (fitnessValues[j] < bestFitnessValues[j]) {
                        bestFitnessValues[j] = fitnessValues[j];
                        System.arraycopy(bestSchedules[j], 0, schedules[j], 0, m * n);
                    }
                }
                //ystem.out.println("\nBest fit iteration :"+i );
            }
            return bestFitnessValues;
        }

        // Get the best schedule
        void  getBestSchedule() {
            int[][] bestSchedule = new int[m][n];
            for (int i = 0; i < m; i++) {
                System.arraycopy(schedules[i], 0, bestSchedule[i], 0, n);
            }
            /*for(int j=0;j<schedules[0].length;j++)
            	System.out.print(schedules[0][j]+" ");
            System.out.println("\n");*/
        }
}
