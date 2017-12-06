package repast;

import agents.Driver;
import agents.Park;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import jade.wrapper.StaleProxyException;

public class ParkingModel extends Repast3Launcher {
	private static final int NUMPARKS = 10;
	private static final int NUMDRIVERS = 25; // maybe moar idk xD
	private static final int WORLDXSIZE = 420;
	private static final int WORLDYSIZE = 420;
	private static final int AGENT_LIFESPAN = 30;

	private int numParks = NUMPARKS;
	private int numDrivers = NUMDRIVERS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int agentLifeSpan = AGENT_LIFESPAN;
	
	private ContainerController mainContainer;
	
	private Schedule schedule;
	
	@Override
	public void begin() {
		super.begin();
		buildModel();
		buildSchedule();
		buildDisplay();
	}

	public void buildModel() {
		System.out.println("Running buildModel...");
	}

	public void buildSchedule() {
		System.out.println("Running buildSchedule...");
	}

	public void buildDisplay() {
		System.out.println("Running buildDisplay...");
	}
	
	@Override
	public Schedule getSchedule() {
		return schedule;
	}

	@Override
	public String[] getInitParam() {
		String[] initParams = { "NumParks", "NumDrivers", "WorldXSize", "WorldYSize", "AgentLifespan"};
	    return initParams;
	}

	@Override
	public String getName() {
		return "Parking Simulation";
	}

	@Override
	public void setup() {
		super.setup();
		System.out.println("Running setup...");
	}
	
	
	
	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		launchAgents();
	}
	
	private Park createNewPark(){
		// static, 10, 3, 49.3, 54.1
		Object[] args = new Object[8];
		return new Park(args);
	}
	
	private void launchAgents() {
		try{
			for(int i = 0; i < numParks; i++){
				Park p = createNewPark();
				mainContainer.acceptNewAgent("Park " + i, p);
			}
			
		} catch (StaleProxyException e){
			e.printStackTrace();
		}
	
		int N_CONSUMERS = N;
		int N_CONSUMERS_FILTERING_PROVIDERS = N;
		int N_PROVIDERS = 2*N;
		
		try {
			
			AID resultsCollectorAID = null;
			if(USE_RESULTS_COLLECTOR) {
				// create results collector
				ResultsCollector resultsCollector = new ResultsCollector(N_CONSUMERS + N_CONSUMERS_FILTERING_PROVIDERS);
				mainContainer.acceptNewAgent("ResultsCollector", resultsCollector).start();
				resultsCollectorAID = resultsCollector.getAID();
			}
			
			// create providers
			// good providers
			for (int i = 0; i < N_PROVIDERS/2; i++) {
				ProviderAgent pa = new ProviderAgent(FAILURE_PROBABILITY_GOOD_PROVIDER);
				agentContainer.acceptNewAgent("GoodProvider" + i, pa).start();
			}
			// bad providers
			for (int i = 0; i < N_PROVIDERS/2; i++) {
				ProviderAgent pa = new ProviderAgent(FAILURE_PROBABILITY_BAD_PROVIDER);
				agentContainer.acceptNewAgent("BadProvider" + i, pa).start();
			}

			// create consumers
			// consumers that use all providers
			for (int i = 0; i < N_CONSUMERS; i++) {
				ConsumerAgent ca = new ConsumerAgent(N_PROVIDERS, N_CONTRACTS, resultsCollectorAID);
				mainContainer.acceptNewAgent("Consumer" + i, ca).start();
			}
			// consumers that filter providers
			for (int i = 0; i < N_CONSUMERS_FILTERING_PROVIDERS; i++) {
				ConsumerAgent ca = new ConsumerAgent(FILTER_SIZE, N_CONTRACTS, resultsCollectorAID);
				mainContainer.acceptNewAgent("ConsumerF" + i, ca).start();
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new ParkingModel(), "", false);
	}
	
	public int getNumParks() {
		return numParks;
	}

	public void setNumParks(int numParks) {
		this.numParks = numParks;
	}

	public int getNumDrivers() {
		return numDrivers;
	}

	public void setNumDrivers(int numDrivers) {
		this.numDrivers = numDrivers;
	}

	public int getAgentLifeSpan() {
		return agentLifeSpan;
	}

	public void setAgentLifeSpan(int agentLifeSpan) {
		this.agentLifeSpan = agentLifeSpan;
	}

	public int getWorldXSize() {
		return worldXSize;
	}

	public void setWorldXSize(int worldXSize) {
		this.worldXSize = worldXSize;
	}

	public int getWorldYSize() {
		return worldYSize;
	}

	public void setWorldYSize(int worldYSize) {
		this.worldYSize = worldYSize;
	}

}
