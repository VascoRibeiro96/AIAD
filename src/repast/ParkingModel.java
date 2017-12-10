package repast;

import java.awt.Color;
import java.util.ArrayList;

import agents.Driver;
import agents.Park;
import agents.SimulationController;
import jade.core.Profile;
import jade.core.ProfileImpl;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import jade.wrapper.StaleProxyException;
import uchicago.src.sim.analysis.DataSource;

import uchicago.src.sim.analysis.Sequence;

public class ParkingModel extends Repast3Launcher {
    private static final int NUMPARKS = 10;
    private static final int NUMDRIVERS = 25;
    private static final int WORLDXSIZE = 100;
    private static final int WORLDYSIZE = 100;
    private static final int AGENT_LIFESPAN = 30;

    private int numParks = NUMPARKS;
    private int numDrivers = NUMDRIVERS;
    private int worldXSize = WORLDXSIZE;
    private int worldYSize = WORLDYSIZE;
    private int agentLifeSpan = AGENT_LIFESPAN;
    private int displayParkChart = 1;
    protected ArrayList<Park> parkList;
    protected ArrayList<Driver> driverList;

    protected ContainerController mainContainer;

    protected ParkingSpace pkspc;
    private DisplaySurface displaySurf;

    private Schedule schedule;
    private OpenSequenceGraph amountOfMoneyInPark;

    class moneyInPark implements DataSource, Sequence {

        public Object execute() {
            return new Double(getSValue());
        }

        public double getSValue() {
            return (double)pkspc.getTotalMoney("Park " + displayParkChart);
        }
    }

    @Override
    public void begin() {
        super.begin();
        buildModel();
        buildSchedule();
        buildDisplay();
        displaySurf.display();
        amountOfMoneyInPark.display();
        //   agentWealthDistribution.display();
    }

    private void buildModel() {
        System.out.println("Running buildModel...");
    }

    private void buildSchedule() {
        System.out.println("Running buildSchedule...");
        class UpdateBoard extends BasicAction {
            public void execute() {
                displaySurf.updateDisplay();
            }
        }
        schedule.scheduleActionBeginning(0,new UpdateBoard());

        class CarryDropUpdateMoneyInPark extends BasicAction {
            public void execute(){
                amountOfMoneyInPark.step();
            }
        }

        schedule.scheduleActionAtInterval(10, new CarryDropUpdateMoneyInPark());
    }
    private void buildDisplay() {
        System.out.println("Running buildDisplay...");
        ColorMap map = new ColorMap();
        map.mapColor(0, Color.white);
        map.mapColor(1, Color.white);
        map.mapColor(2, Color.yellow);
        Value2DDisplay displayMap = new Value2DDisplay(pkspc.getMapSpace(), map);
        Object2DDisplay displayDrivers = new Object2DDisplay(pkspc.getDriverSpace());
        displayDrivers.setObjectList(driverList);
        displaySurf.addDisplayableProbeable(displayMap, "Map");
        displaySurf.addDisplayableProbeable(displayDrivers, "Drivers");
        amountOfMoneyInPark.addSequence("Money In Park", new moneyInPark());
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public String[] getInitParam() {
        return new String[]{ "NumParks", "NumDrivers", "WorldXSize", "WorldYSize", "AgentLifespan", "DisplayParkChart"};
    }

    @Override
    public String getName() {
        return "Parking Simulation";
    }

    @Override
    public void setup() {
        System.out.println("Running setup...");
        pkspc = null;
        parkList = new ArrayList<>();
        driverList = new ArrayList<>();
        if (displaySurf != null){
            displaySurf.dispose();
        }
        displaySurf = null;

        if (amountOfMoneyInPark != null){
            amountOfMoneyInPark.dispose();
        }
        amountOfMoneyInPark = null;

        displaySurf = new DisplaySurface(this, "Carry Drop Model Window 1");
        amountOfMoneyInPark = new OpenSequenceGraph("Amount Of Money In Park",this);

        registerDisplaySurface("Carry Drop Model Window 1", displaySurf);
        this.registerMediaProducer("Plot", amountOfMoneyInPark);
        super.setup();
        schedule = super.getSchedule();


    }

    @Override
    protected void launchJADE() {
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

    public int getDisplayParkChart() {
        return displayParkChart;
    }

    public void setDisplayParkChart(int displayParkChart) {
        this.displayParkChart = displayParkChart;
    }
}
