import java.util.*;

public class RoundRobin
{
    private int OS_DECISION_TIMESLICE = 20;
    private double PROCESS_GENERATE_PROBABILITY = 0.97;
    private double IO_EVENT_GENERATE_PROBABILITY = 0.97;
    private int SIMULATION_TIME = 1500;

    private boolean FOR_TEST = false;
    private boolean OUTPUT_LOG = false;

    private int num_of_completed_Jobs = 0;
    private Job currentJob = null;
    private long total_latency_for_served_Jobs;

    private double totalEnergyUsed;

    private void Test(int i, PriorityQueue<Job> pQ)
    {
        if (i == 0)
        {
            Job j1 = new Job(i, IO_EVENT_GENERATE_PROBABILITY);
            j1.setArrivaltimestamp(0);
            j1.setJobLength(7);
            pQ.add(j1);

            if (OUTPUT_LOG)
                System.out.println("CREATED: " + j1.toString());
        }
        else if(i == 2)
        {
            Job j2 = new Job(i, IO_EVENT_GENERATE_PROBABILITY);
            j2.setArrivaltimestamp(2);
            j2.setJobLength(4);
            pQ.add(j2);

            if (OUTPUT_LOG)
                System.out.println("CREATED: " + j2.toString());
        }
        else if(i == 4)
        {
            Job j3 = new Job(i, IO_EVENT_GENERATE_PROBABILITY);
            j3.setArrivaltimestamp(4);
            j3.setJobLength(1);
            pQ.add(j3);

            if (OUTPUT_LOG)
                System.out.println("CREATED: " + j3.toString());
        }
        else if (i == 5)
        {
            Job j4 = new Job(i, IO_EVENT_GENERATE_PROBABILITY);
            j4.setArrivaltimestamp(5);
            j4.setJobLength(4);
            pQ.add(j4);

            if (OUTPUT_LOG)
                System.out.println("CREATED: " + j4.toString());
        }
    }

    public RoundRobin()
    {
    }

    public RoundRobin(int SIMULATION_TIME, int OS_DECISION_TIMESLICE, double PROCESS_GENERATE_PROBABILITY, double IO_EVENT_GENERATE_PROBABILITY, boolean OUTPUT_LOG, boolean FOR_TEST)
    {
        this.SIMULATION_TIME = SIMULATION_TIME;
        this.OS_DECISION_TIMESLICE = OS_DECISION_TIMESLICE;
        this.PROCESS_GENERATE_PROBABILITY = PROCESS_GENERATE_PROBABILITY;
        this.IO_EVENT_GENERATE_PROBABILITY = IO_EVENT_GENERATE_PROBABILITY;
        this.OUTPUT_LOG = OUTPUT_LOG;
        this.FOR_TEST = FOR_TEST;
    }

    private void UpdateTotalEnergyUsed(PriorityQueue<Job> pQ, Job currentJob)
    {
        Iterator<Job> it =  pQ.iterator();
        while(it.hasNext())
            totalEnergyUsed += ((Job) it.next()).consumedEnergy();

        if (currentJob != null)
            totalEnergyUsed += currentJob.remainingExecutionTime() > 0 ? currentJob.consumedEnergy() : 0;
    }

    private void ProcessIO_Queue(PriorityQueue<Job> pQ, Queue<Job> queue)
    {
        if (queue.size() > 0)
        {
            Job head = (Job) queue.element();

            if (head.remainingIOTime() > 0)
            {
                //Perform IO Event of Head of Queue
                head.decrementIOEventLength();

                if (OUTPUT_LOG)
                    System.out.println("PERFORMING I/O: " + head.toString());

                // IO Event finishes of the Head Job in IO Queue, so add back it to readyQueue
                if (head.remainingIOTime() == 0)
                {
                    pQ.add(queue.remove());
                }
            }

            //Update Waiting time of all jobs in IO Queue
            Iterator<Job> it =  queue.iterator();
            while(it.hasNext())
                ((Job) it.next()).incrementWaitTime();
        }
    }

    private void UpdateWaitingTime(PriorityQueue<Job> pQ)
    {
        Iterator<Job> it =  pQ.iterator();
        while(it.hasNext())
            ((Job) it.next()).incrementWaitTime();
    }

    public void RunSimulation(Constants.RR_STRATEGY strategy) throws InterruptedException
    {
        PriorityQueue<Job> processReadyPQ = new PriorityQueue<Job>();
        Queue<Job> processIO_Queue = new LinkedList<>();

        int sliceCounter = -1;

        for(int CurrentTimeStamp = 0; CurrentTimeStamp < SIMULATION_TIME; CurrentTimeStamp++)
        {
            sliceCounter++;

            Thread.sleep(1);

            if(FOR_TEST)
            {
                Test(CurrentTimeStamp, processReadyPQ);
            }
            else
            {
                if(canGenerateProcess())
                {
                    Job obj = new Job(CurrentTimeStamp, IO_EVENT_GENERATE_PROBABILITY);
                    obj.setStrategy(strategy);
                    processReadyPQ.add(obj);

                    if (OUTPUT_LOG)
                        System.out.println("CREATED: " + obj.toString());
                }
            }

            if (sliceCounter % OS_DECISION_TIMESLICE == 0 ||(sliceCounter % OS_DECISION_TIMESLICE != 0 && currentJob != null && currentJob.remainingExecutionTime() <= 0))
            {
                //currentJob finishes and hence calculate its total energy consumed
                if (currentJob != null && currentJob.remainingExecutionTime() <= 0)
                    totalEnergyUsed += currentJob.consumedEnergy();

                //currentJob finished but the time slice allocated for it completed
                if (currentJob != null && currentJob.remainingExecutionTime() > 0)
                    processReadyPQ.add(currentJob);

                if(processReadyPQ.size() > 0)
                {
                    //CurrentStat(pQ);
                    currentJob = processReadyPQ.remove();
                    currentJob.setLatency(CurrentTimeStamp - currentJob.getLastServedTime());
                    total_latency_for_served_Jobs += currentJob.getLatency();
                    currentJob.setLastServedTime(CurrentTimeStamp);
                }
            }

            if(currentJob != null && currentJob.remainingExecutionTime() > 0)
            {
                //NOTE that currentJob at this point never belongs to IO_Queue, so it never has IO Event at this point
                if(currentJob.hasIOEvent())
                {
                    sliceCounter = 0;
                    processIO_Queue.add(currentJob);
                    currentJob = null;
                }
                else
                {
                    currentJob.decrementJobLength();
                    currentJob.setLastServedTime(CurrentTimeStamp);

                    if (OUTPUT_LOG)
                        System.out.println("EXECUTING: " + currentJob.toString());

                    if(currentJob.remainingExecutionTime() == 0)
                    {
                        sliceCounter = 0;

                        if (OUTPUT_LOG)
                            System.out.println(" > COMPLETED: " + currentJob.toString());

                        num_of_completed_Jobs++;
                    }
                }
            }

            //Update waiting time for all other processes JobQueue
            UpdateWaitingTime(processReadyPQ);

            //Process any IO events at every tick if available
            ProcessIO_Queue(processReadyPQ, processIO_Queue);
        }


        UpdateTotalEnergyUsed(processReadyPQ, currentJob);

        System.out.println("Total Jobs: " + Job.getNumOfJobsGenerated());
        System.out.println("Number of Completed Jobs = " + num_of_completed_Jobs);
        System.out.println("Throughput = " + getThroughput());
        System.out.println("Avg Latency = " + (getAverageLatency()==-1?"No Completed Jobs":getAverageLatency()));

        if (strategy.equals(Constants.RR_STRATEGY.HECN))
            System.out.println("Energy Dissipated = " + totalEnergyUsed);
    }


    private double getThroughput()
    {
        return ((double) num_of_completed_Jobs / SIMULATION_TIME);
    }

    private double getAverageLatency()
    {
        return num_of_completed_Jobs > 0 ?((double) total_latency_for_served_Jobs / num_of_completed_Jobs) : -1;
    }

    private boolean canGenerateProcess()
    {
        Random rand = new Random(System.currentTimeMillis());
        int rand_int1 = rand.nextInt(101);
        if((rand_int1 / (double) 100 ) > PROCESS_GENERATE_PROBABILITY)
            return true;
        else
            return false;
    }
}