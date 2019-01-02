import java.util.Random;

public class Job implements Comparable<Job>
{
    private double IO_EVENT_GENERATE_PROBABILITY = 0.97;
    private static int numOfJobsGenerated = 0;

    private int processJobID;
    private long arrivalTimestamp;

    private int origJobLength;
    private int jobLength;
    private int IOLength;
    private long latency;

    private int requiredEnergy;
    private int waitedTime = 0;
    private Constants.RR_STRATEGY currentStrategy = Constants.RR_STRATEGY.HRRN;

    private long lastServedTime = 0;

    public Job(int currentTimeStamp, double IO_EVENT_GENERATE_PROBABILITY)
    {
        this.jobLength = getRandomNumber(10, 50);

        this.origJobLength = this.jobLength;
        this.requiredEnergy = getRandomNumber(10, 30);

        Job.numOfJobsGenerated++;
        this.processJobID = numOfJobsGenerated;
        this.arrivalTimestamp = currentTimeStamp;
        this.IO_EVENT_GENERATE_PROBABILITY = IO_EVENT_GENERATE_PROBABILITY;
    }

    public boolean hasIOEvent()
    {
        if(this.IOLength > 0)
        {
            return true;
        }
        else if(canGenerateIOEvent())
        {
            this.IOLength = getRandomNumber(5, 15);
            return true;
        }
        return false;
    }

    private boolean canGenerateIOEvent()
    {
        int rand_int = getRandomNumber(0, 100);

        if((rand_int / (double) 100) > IO_EVENT_GENERATE_PROBABILITY)
            return true;
        else
            return false;
    }

    public void incrementWaitTime()
    {
        this.waitedTime++;
    }

    public double consumedEnergy()
    {
        return (this.origJobLength != this.jobLength) ? this.requiredEnergy * ( (double) (this.origJobLength - this.jobLength) / (double) this.origJobLength) : 0;
    }

    public void decrementJobLength()
    {
        this.jobLength--;
    }

    public void decrementIOEventLength()
    {
        this.IOLength--;
    }

    public double getHRRN_Priority()
    {
        return ((double) this.jobLength > 0) ? (double) (this.waitedTime + this.jobLength) / (double) this.jobLength : 0;
    }

    public double getHECN_Priority()
    {
        return ((double) (this.jobLength * this.requiredEnergy) > 0) ? (double) (this.waitedTime + this.jobLength *  this.requiredEnergy) / (double) (this.jobLength * this.requiredEnergy) : 0;
    }

    @Override
    public String toString()
    {
        return "Job ID = " + this.processJobID +
                " Current Job Length = " + this.jobLength +
                " Current IO length = " + this.IOLength +
                " WAIT TIME = " + this.waitedTime +
                " HRRN = " + this.getHRRN_Priority() +
                " HECN = " + this.getHECN_Priority();
    }

    @Override
    public int compareTo(Job job)
    {
        if (currentStrategy.equals(Constants.RR_STRATEGY.HRRN))
        {
            if(job.getHRRN_Priority() == this.getHRRN_Priority())
            {
                if(job.jobLength > this.jobLength)
                    return -1;
                else
                    return 1;
            }
            else if(job.getHRRN_Priority() > this.getHRRN_Priority())
                return 1;
            else
                return -1;
        }
        else if (currentStrategy.equals(Constants.RR_STRATEGY.HECN))
        {
            if(job.getHECN_Priority() == this.getHECN_Priority())
            {
                if(job.jobLength > this.jobLength)
                    return -1;
                else
                    return 1;
            }
            else if(job.getHECN_Priority() > this.getHECN_Priority())
                return 1;
            else
                return -1;
        }
        else
        {
            return 0;
        }
    }

    // Number of Jobs Generated getter setter
    public static int getNumOfJobsGenerated()
    {
        return numOfJobsGenerated;
    }

    // Last Served Time Getter Setter
    public long getLastServedTime()
    {
        return lastServedTime;
    }

    public void setLastServedTime(long lastServedTime)
    {
        this.lastServedTime = lastServedTime;
    }

    // Strategy Getter Setter
    public void setStrategy(Constants.RR_STRATEGY strategy)
    {
        this.currentStrategy = strategy;
    }

    // Arrival Time Getter Setter
    public void setArrivaltimestamp(long arrivalTimestamp)
    {
        this.arrivalTimestamp = arrivalTimestamp;
    }

    public long getArrivalTimestamp()
    {
        return arrivalTimestamp;
    }

    // Latency Getter Setter
    public void setLatency(long latency)
    {
        this.latency = latency;
    }

    public long getLatency()
    {
        return latency;
    }

    // Job Length Getter Setter
    public void setJobLength(int jobLength)
    {
        this.jobLength = jobLength;
    }

    public int remainingExecutionTime()
    {
        return this.jobLength;
    }

    // IO Length Getter Setter
    public void setIOLength(int IOLength)
    {
        this.IOLength = IOLength;
    }

    public int remainingIOTime()
    {
        return this.IOLength;
    }

    // Helper Functions
    private int getRandomNumber(int lowerLimit, int upperLimit)
    {
        Random rand = new Random(System.currentTimeMillis());
        return rand.nextInt(upperLimit - lowerLimit + 1) + lowerLimit;
    }
}