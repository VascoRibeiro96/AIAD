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


// Experiencia para testar parque dimâmico, mais especificamente para testar valores para a inflação
public class Experience6 extends ParkingModel {

    @Override
    public void launchJADE(){
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        mainContainer = rt.createMainContainer(p1);
        pkspc = new ParkingSpace(getWorldXSize(), getWorldYSize());
        launchAgents();
    }

    private Park createNewPark(){
        Object[] args = new Object[8];
        args[0] = "dynamic";
        args[1] = "10"; // preco por h
        args[2] = "10"; // nº total de lugares
        args[3] = "10"; // isto e o y é melhor ser em Inteiro porcausa da grelha
        args[4] = "10"; //
        args[5] = "1"; // learn rate
        args[6] = "0.1"; // inflação por hora em percentagem
        args[7] = "200"; // percentagem de alteração de preço diára
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
        args[5] = "15"; // max dinheiro a pagar por hora
        args[6] = "3"; // distancia maxima a andar a pé
        args[7] = "1"; // tempo de estacionamento
        return new Driver(args, getNumParks());
    }

    private void launchAgents() {
        try{
            Park p = createNewPark();
            if(pkspc.simpleAddPark(p)){
                parkList.add(p);
                mainContainer.acceptNewAgent("Park 1" , p).start();
            }
            for(int i = 0; i < getNumDrivers(); i++){
                Driver d = createNewDriver();
                d.setTimePark(i+1);
                pkspc.simpleAddDriver(d);
                driverList.add(d);
                mainContainer.acceptNewAgent("Driver " + i, d).start();

            }
            SimulationController sm = new SimulationController(getNumParks(),getNumDrivers(),200,1);
            mainContainer.acceptNewAgent("SimulationController 1", sm).start();
        } catch (StaleProxyException e){
            e.printStackTrace();
        }
    }
}
