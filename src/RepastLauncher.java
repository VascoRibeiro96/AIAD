import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.Schedule;

public class RepastLauncher {

	private Schedule schedule;
	private int numAgents;
	private int worldXSize;
	private int worldYSize;

	public String getName() {
		return "My First RePast Model";
	}

	public void setup() {
	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();
	}

	public void buildModel() {
	}

	public void buildSchedule() {
	}

	public void buildDisplay() {
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
	}
}
