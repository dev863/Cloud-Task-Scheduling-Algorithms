package ESFO;


import java.util.Arrays;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;

import utils.Constants;
import utils.GenerateMatrices;

public class FitnessFunction  {
    private static double[][] execMatrix, commMatrix;
    private static int NUM_VMS=50;
    public FitnessFunction(double [][] processingTimes) {
       
        commMatrix = GenerateMatrices.getCommMatrix();
        execMatrix = processingTimes;
      }
    
	public static double calculateFitness(int[] offspring,double[][] processingTimes, int m, int n) {
        double[] machineTime = new double[m];
        double[] jobTime = new double[m];
        for (int i = 0; i < m; i++) {
            int job = offspring[i];
            jobTime[job] += processingTimes[job][i];
            for (int j = 0; j < m; j++) {
                if (j == 0) {
                    machineTime[j] += processingTimes[job][i];
                } else {
                    machineTime[j] = Math.max(machineTime[j], machineTime[j - 1]) + processingTimes[job][i];
                }
            }
        }
        double maxTime = Arrays.stream(machineTime).max().getAsDouble();
        double averageTime = Arrays.stream(jobTime).average().getAsDouble();
        return 1.0 / (maxTime + averageTime);
    }

    public double calcMakespan(double[] position) {
        double makespan = 0;
        double[] dcWorkingTime = new double[NUM_VMS];

        for (int i = 0; i < NUM_VMS; i++) {
            int dcId = ((int) position[i])%50;
           
            dcWorkingTime[dcId] += execMatrix[i][dcId] + commMatrix[i][dcId];
            makespan = Math.max(makespan, dcWorkingTime[dcId]);
        }
        return makespan;
    }
}
