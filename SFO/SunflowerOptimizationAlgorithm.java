

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;



public class SunflowerOptimizationAlgorithm {
	private static double pollination_factor = 0.5;
	private static double p = 0.5;
    private int N;  // number of tasks
    private int M;  // number of VMs
    private int K;  // number of iterations
    private double[] C;  // computational cost of each task
    private double[] R;  // resource requirement of each task
    private double[][] B; // bandwidth matrix between tasks
    private double[] X;  // solution vector (VM assignment)
    private double[] Xbest;  // best solution vector found so far
    public double Fbest=50000.0;  // objective function value of the best solution
    private Random rand;  // random number generator
    public SunflowerOptimizationAlgorithm(int n, int m, int k, double[] c, double[] r, double[][] b,
            double pollinationFactor, double pollinationFraction) {
N = n;
M = m;
K = k;
C = c;
R = r;
B = b;
X = new double[N];
Xbest = new double[N];
Fbest = Double.MAX_VALUE;
rand = new Random();
pollination_factor = pollinationFactor;
p = pollinationFraction;
}
    
    public void solve() {
        // initialize solution vector randomly
    	for (int i = 0; i < N; i++) {
            X[i] = rand.nextInt(M);
        }
       
        // main loop
        for (int k = 0; k < K; k++) {
            // calculate fitness value of current solution
            double fitness = calculateFitness(X);
            
            // check if current solution is the best found so far
            if (fitness < Fbest) {
                Fbest = fitness;
                System.arraycopy(X, 0, Xbest, 0, N);
            }
            //System.out.println("\nFitness value for iteration "+(k+1)+" is: "+Fbest);
            // generate new candidate solutions
            ArrayList<double[]> candidates = generateCandidates(X);
                
                // select the best candidate solution
                double[] Xnew = selectBestCandidate(candidates);
                
                // update solution vector
                System.arraycopy(Xnew, 0, X, 0, N);
           	
        }
    }
    
    private double calculateFitness(double[] X) {
        double fitness = 0;
        for (int i = 0; i < N; i++) {
            int vm = (int) X[i];
            fitness += C[i] / (1 - R[vm]);
            for (int j = 0; j < N; j++) {
                if (i != j) {
                    int vm1 = (int) X[i];
                    int vm2 = (int) X[j];
                    fitness += B[i][j] / (1 - R[vm1]) / (1 - R[vm2]);
                  
                }
            }
        }
       
        return fitness;
    }
    
    private ArrayList<double[]> generateCandidates(double[] X) {
        ArrayList<double[]> candidates = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (X[i] != j) {
                    double[] Xnew = X.clone();
                    Xnew[i] = j;
                    candidates.add(Xnew);
                }
            }
        }

        // pollination
        int numPollinations = (int) (pollination_factor * candidates.size());
        for (int i = 0; i < numPollinations; i++) {
            int index1 = rand.nextInt(candidates.size());
            int index2 = rand.nextInt(candidates.size());
            double[] X1 = candidates.get(index1);
            double[] X2 = candidates.get(index2);
            double[] Xnew = pollinate(X1, X2);
            candidates.add(Xnew);
        }

        return candidates;
    }
    private double[] pollinate(double[] X1, double[] X2) {
        double[] Xnew = X1.clone();
        int numPollinatedTasks = (int) (p * N);
        for (int i = 0; i < numPollinatedTasks; i++) {
            int index = rand.nextInt(N);
            Xnew[index] = X2[index];
        }
        return Xnew;
    }
    
    private double[] selectBestCandidate(ArrayList<double[]> candidates) {
        Collections.shuffle(candidates);
        double[] Xbest = candidates.get(0);
        double Fbest = calculateFitness(Xbest);
        for (int i = 1; i < candidates.size(); i++) {
            double[] Xnew = candidates.get(i);
            double Fnew = calculateFitness(Xnew);
            if (Fnew < Fbest) {
                Xbest = Xnew;
                Fbest = Fnew;
            }
        }
        return Xbest;
    }

	public double[] getXbest() {
		return Xbest;
	}
	public static double calculateEnergyConsumption(List<Cloudlet> cloudletList, List<Vm> vmList) {
        double energy = 0;

        for (Cloudlet cloudlet : cloudletList) {
            double runtime = cloudlet.getActualCPUTime();
            double utilization = runtime / cloudlet.getFinishTime();

            Vm vm = vmList.get(cloudlet.getVmId()); // retrieve the VM object associated with the cloudlet
            double power =getPowerModel(vm).getPower(utilization) ;

            energy += power * runtime;
        }

        return energy;
    }
	public static PowerModel getPowerModel(Vm vm) {
        double utilizationThresholds =0.8;
        double powerValues = 50;

        PowerModelLinear powerModel = new PowerModelLinear(utilizationThresholds, powerValues);

        return powerModel;
    }
}
