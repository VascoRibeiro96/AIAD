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
		// super.begin();
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
		// super.setup();
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
		// tipo (static ou dynamic), preco, nº lugares, x, y
		// static, 10, 3, 49.3, 54.1
		Object[] args = new Object[5];
		args[0] = "static";
		args[1] = "10"; // preco por h
		args[2] = "3"; // nº total de lugares
		args[3] = "2"; // isto e o y é melhor ser em Inteiro porcausa da grelha
		args[4] = "5"; // 
		return new Park(args);
	}
	
	private Driver createNewDriver(){
		// args: tipo de driver(explorer, rational), xi, yi, xf, yf, maxMoney, maxDist, timePark
		// explorer, 49.3, 49.4, 65.12, 12.2, 25, 100, 2
		Object[] args = new Object[8];
		args[0] = "rational"; // tipo
		args[1] = "32"; //xi
		args[2] = "32"; // yi
		args[3] = "47"; // xf
		args[4] = "47"; // yf
		args[5] = "25"; // max dinheiro a pagar por hora
		args[6] = "100"; // distancia maxima a andar a pé
		args[7] = "2"; // tempo de estacionamento
		return new Driver(args);
	}
	
	private void launchAgents() {
		try{
			for(int i = 0; i < numParks; i++){
				Park p = createNewPark();
				mainContainer.acceptNewAgent("Park " + i, p).start();
			}
			for(int i = 0; i < numDrivers; i++){
				Driver d = createNewDriver();
				mainContainer.acceptNewAgent("Driver " + i, d).start();
			}
		} catch (StaleProxyException e){
			e.printStackTrace();
		}
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
