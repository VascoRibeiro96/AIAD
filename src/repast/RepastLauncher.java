package repast;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;

/*
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
*/

public class RepastLauncher extends SimModelImpl {
	// Default Values
	private static final int NUMAGENTS = 100;
	private static final int WORLDXSIZE = 80;
	private static final int WORLDYSIZE = 80;

	private Schedule schedule;
	private int numAgents = NUMAGENTS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;

	public String getName() {
		return "Parques de Estacionamento";
	}

	public void setup() {
		System.out.println("Running setup...");
	}

	public void begin() {
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

	public Schedule getSchedule() {
		return schedule;
	}

	public String[] getInitParam() {
		String[] initParams = { "NumAgents", "WorldXSize", "WorldYSize" };
		return initParams;
	}

	public int getNumAgents() {
		return numAgents;
	}

	public void setNumAgents(int na) {
		numAgents = na;
	}

	public int getWorldXSize() {
		return worldXSize;
	}

	public void setWorldXSize(int wxs) {
		worldXSize = wxs;
	}

	public int getWorldYSize() {
		return worldYSize;
	}

	public void setWorldYSize(int wys) {
		worldYSize = wys;
	}

	public static void main(String[] args) {
		SimInit init = new SimInit();
		RepastLauncher model = new RepastLauncher();
		init.loadModel(model, "", false);
	}
	
/*
	protected void launchJADE() {
		
		System.out.println("Launching jade...");

		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		System.out.println(rt.toString());
		mainContainer = rt.createMainContainer(p1);
		
		//mainContainer = rt.createAgentContainer(p1);

		launchAgents();
	}
	
	
	public Context build(Context<Object> context) {
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("Service Consumer/Provider network", context, true);
		netBuilder.buildNetwork();
		
		return super.build(context);
	}

	public void launchAgents() {

		try {
			// create explorer driver agents
			for (int i = 0; i < N_EXPLORER_DRIVERS; i++) {
				ExplorerDriverAgent ed = new ExplorerDriverAgent();
				mainContainer.acceptNewAgent("ExplorerDriver" + i, ed).start();
			}

			// create guided driver agents
			for (int i = 0; i < N_GUIDED_DRIVERS; i++) {
				ExplorerDriverAgent gd = new ExplorerDriverAgent();
				mainContainer.acceptNewAgent("GuidedDriver" + i, gd).start();
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
*/
}
