package ESFO;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.core.CloudSim;

import ESFO.FitnessFunction;
import utils.Constants;

import utils.GenerateMatrices;

import java.text.DecimalFormat;
import java.util.*;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
public class ESFO_Scheduler {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static int NUM_VMS=50;
    private static int NUM_TASKS=100;
    private static Datacenter[] datacenter;
    private static ESFO ESFOSchedularInstance;
    private static double bestfitnessValues[];
    private static double[][] commMatrix;
    private static double[][] execMatrix;
    //private static FitnessFunction ff = new FitnessFunction();
    static Random rand=new Random();
    
    public static void main(String[] args) {
        Log.printLine("Starting ESFO Scheduler...");
        
        double[][] processingTimes=new double[Constants.NO_OF_TASKS][NUM_VMS];
        for(int i=0;i<NUM_TASKS;i++)
        	for(int j=0;j<NUM_VMS;j++)
        		processingTimes[i][j]=rand.nextDouble(2.5*NUM_TASKS);				
        new GenerateMatrices();
        FitnessFunction ff=new FitnessFunction(processingTimes);
        commMatrix = GenerateMatrices.getCommMatrix();
        execMatrix = processingTimes;//GenerateMatrices.getExecMatrix();
        ESFOSchedularInstance = new ESFO(NUM_TASKS,NUM_VMS,100,0.5,30,processingTimes);
        bestfitnessValues=ESFOSchedularInstance.run();
        
      try {
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			vmList = new ArrayList<Vm>();
			Vm [] vm=new Vm[NUM_VMS];
			// VM description
			int vmid = 0;
			int mips = 1000;
			long size = 10000; // image size (MB)
			int ram = 256; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name
			for(int i=0;i<NUM_VMS;i++) {
			// create VM
			 vm[i] = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			vmList.add(vm[i]);
			}
			broker.submitVmList(vmList);

			// Fifth step: Create one Cloudlet
			cloudletList = new ArrayList<Cloudlet>();

			// Cloudlet properties
			int id = 0;
			long length = 400000;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();
            Cloudlet [] cloudlet =new Cloudlet[NUM_TASKS];
            for(int i=0;i<NUM_TASKS;i++)
            {	
            	cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            	cloudlet[i].setUserId(brokerId);
            	
    			//
            	cloudlet[i].setVmId(i%50);
    			cloudletList.add(cloudlet[i]);

            }
			

			// add the cloudlet to the list
			
			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);
            

            //Fourth step: Create VMs and Cloudlets and send them to broker
            
			
            // mapping our dcIds to cloudsim dcIds
			for (int i = 0; i < NUM_TASKS; i++) {
                Cloudlet task = cloudletList.get(i);
                int vmId = (int) bestfitnessValues[i];
                Vm vm1 = vmList.get(vmId%50);
                broker.bindCloudletToVm(task.getCloudletId(), vm1.getId());
                
            }


            // Fifth step: Starts the simulation
            CloudSim.startSimulation();
            
            //List<Cloudlet> newList = broker.getCloudletReceivedList();
            
            List<Cloudlet> finishedTasks = broker.getCloudletReceivedList();
            System.out.println(finishedTasks.size());
            List<Cloudlet> finishedList=new ArrayList<>();
            for(Cloudlet cloudlet1: finishedTasks) {
            	if(cloudlet1.getStatus()==Cloudlet.SUCCESS) {
            		finishedList.add(cloudlet1);
            	}
            }
            CloudSim.stopSimulation();
            Log.printLine("Results:");
            for (int i = 0; i < finishedList.size(); i++) {
                Cloudlet task = finishedList.get(i);
                Log.printLine("Task " + task.getCloudletId() + ":");
                Log.printLine(" - Execution time: " + task.getActualCPUTime());
                Log.printLine(" - VM assigned: " + task.getVmId());
                //Log.printLine(" - Host assigned: " + vmlist.getById(broker.getVmList(), task.getVmId()).getHost().getId());
            }
            //printCloudletList(newList);

            Log.printLine(ESFO_Scheduler.class.getName() + " finished!");
            ESFOSchedularInstance.getBestSchedule();
            System.out.println("Makespan:"+ff.calcMakespan(bestfitnessValues)/10);
            double energyConsumption = calculateEnergyConsumption(cloudletList,vmList);
            System.out.println("Energy Consumption: " + energyConsumption);
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    

    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
   
    public static PowerModel getPowerModel(Vm vm) {
        double utilizationThresholds =0.8;
        double powerValues = 50;

        PowerModelLinear powerModel = new PowerModelLinear(utilizationThresholds, powerValues);

        return powerModel;
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
    private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 125000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 12800; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 50000;

		hostList.add(
			new Host(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerTimeShared(peList)
			)
		); // This is our machine

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
    private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

}
