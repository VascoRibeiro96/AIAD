package agents.Experiences;

import agents.Driver;
import agents.Park;
import agents.SimulationController;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.ParkingModel;
import repast.ParkingSpace;
import sajas.core.Runtime;


// Experiencia para testar o funcionamento do parque estático e funcionamentos básicos do condutor
public class Experience1 extends ParkingModel {

    @Override
    public void launchJADE(){
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        mainContainer = rt.createMainContainer(p1);
        pkspc = new ParkingSpace(getWorldXSize(), getWorldYSize());
        launchAgents();
    }

    private Park createNewPark(){
        // tipo (static ou dynamic), preco, nº lugares, x, y
        // static, 10, 3, 49.3, 54.1
        Object[] args = new Object[5];
        args[0] = "static";
        args[1] = "10"; // preco por h
        args[2] = "3"; // nº total de lugares
        args[3] = "25"; // isto e o y é melhor ser em Inteiro porcausa da grelha
        args[4] = "25"; //
        return new Park(args);
    }

    private Driver createNewDriver(){
        // args: tipo de driver(explorer, rational), xi, yi, xf, yf, maxMoney, maxDist, timePark
        // explorer, 49.3, 49.4, 65.12, 12.2, 25, 100, 2
        Object[] args = new Object[8];
        args[0] = "rational"; // tipo
        args[1] = "25"; //xi
        args[2] = "0"; // yi
        args[3] = "5"; // xf
        args[4] = "5"; // yf
        args[5] = "25"; // max dinheiro a pagar por hora
        args[6] = "3"; // distancia maxima a andar a pé
        args[7] = "5"; // tempo de estacionamento
        return new Driver(args, getNumParks());
    }

    private void launchAgents() {
        try{
            Park p = createNewPark();
            if(pkspc.simpleAddPark(p)){
                parkList.add(p);
                mainContainer.acceptNewAgent("Park 1" , p).start();
            }
            Driver d = createNewDriver();
            if(pkspc.simpleAddDriver(d)) {
                driverList.add(d);
                mainContainer.acceptNewAgent("Driver 1", d).start();
            }
            SimulationController sm = new SimulationController(getNumParks(),getNumDrivers(),200,1);
            mainContainer.acceptNewAgent("SimulationController 1", sm).start();
        } catch (StaleProxyException e){
            e.printStackTrace();
        }
    }
}
