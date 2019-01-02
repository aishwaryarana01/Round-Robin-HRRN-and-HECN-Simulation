public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        //System.out.println("-------------------------");
        //System.out.println("HRRN Simulation");
        //System.out.println("-------------------------");
        //new RoundRobin().RunSimulation(Constants.RR_STRATEGY.HRRN); // For Running with default configuration for assignment

        //System.out.println("\n-------------------------");
        //System.out.println("HECN Simulation");
        //System.out.println("-------------------------");
        //new RoundRobin().RunSimulation(Constants.RR_STRATEGY.HECN); // For Running with default configuration for assignment

        //For Running Test Process Set
        //new RoundRobin(20, 2, 0.97, 0.97, true, true).RunSimulation(Constants.RR_STRATEGY.HRRN);
    	//new RoundRobin(20, 2, 0.97, 0.97, true, true).RunSimulation(Constants.RR_STRATEGY.HECN);

        System.out.println("-------------------------");
        System.out.println("HRRN Simulation");
        System.out.println("-------------------------");
        new RoundRobin(1500, 20, 0.97, 0.97, false, false).RunSimulation(Constants.RR_STRATEGY.HRRN);

        System.out.println("\n-------------------------");
        System.out.println("HECN Simulation");
        System.out.println("-------------------------");
        new RoundRobin(1500, 20, 0.97, 0.97, false, false).RunSimulation(Constants.RR_STRATEGY.HECN);
    }
}