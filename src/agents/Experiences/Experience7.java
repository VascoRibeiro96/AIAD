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
public class Experience7 extends ParkingModel {

    @Override
    public void launchJADE(){
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        mainContainer = rt.createMainContainer(p1);
        pkspc = new ParkingSpace(getWorldXSize(), getWorldYSize());
        launchAgents();
    }

    private Park createNewDynamicPark1(){
        Object[] args = new Object[8];
        args[0] = "dynamic";
        args[1] = "10";
        args[2] = "5";
        args[3] = "35";
        args[4] = "50";
        args[5] = "1";
        args[6] = "0.0";
        args[7] = "200";
        return new Park(args);
    }

    private Park createNewDynamicPark2(){
        Object[] args = new Object[8];
        args[0] = "dynamic";
        args[1] = "10";
        args[2] = "5";
        args[3] = "50";
        args[4] = "35";
        args[5] = "0.25";
        args[6] = "0.0";
        args[7] = "200";
        return new Park(args);
    }

    private Park createNewDynamicPark3(){
        Object[] args = new Object[8];
        args[0] = "dynamic";
        args[1] = "10";
        args[2] = "5";
        args[3] = "65";
        args[4] = "50";
        args[5] = "0.25";
        args[6] = "0.1";
        args[7] = "125";
        return new Park(args);
    }

    private Park createNewDynamicPark4(){
        Object[] args = new Object[8];
        args[0] = "dynamic";
        args[1] = "20";
        args[2] = "5";
        args[3] = "50";
        args[4] = "65";
        args[5] = "0.25";
        args[6] = "-0.25";
        args[7] = "125";
        return new Park(args);
    }

    private Park createNewPark1(){
        Object[] args = new Object[5];
        args[0] = "static";
        args[1] = "10"; // preco por h
        args[2] = "5"; // nº total de lugares
        args[3] = "60"; // isto e o y é melhor ser em Inteiro porcausa da grelha
        args[4] = "55"; //
        return new Park(args);
    }


    private Park createNewPark2(){
        // tipo (static ou dynamic), preco, nº lugares, x, y
        // static, 10, 3, 49.3, 54.1
        Object[] args = new Object[5];
        args[0] = "static";
        args[1] = "5"; // preco por h
        args[2] = "5"; // nº total de lugares
        args[3] = "55"; // isto e o y é melhor ser em Inteiro porcausa da grelha
        args[4] = "60"; //
        return new Park(args);
    }

    private Driver createNewDriver(){
        // args: tipo de driver(explorer, rational), xi, yi, xf, yf, maxMoney, maxDist, timePark
        // explorer, 49.3, 49.4, 65.12, 12.2, 25, 100, 2
        Object[] args = new Object[8];
        args[0] = "rational"; // tipo
        args[1] = "25"; //xi
        args[2] = "0"; // yi
        args[3] = "50"; // xf
        args[4] = "50"; // yf
        args[5] = "15"; // max dinheiro a pagar por hora
        args[6] = "3"; // distancia maxima a andar a pé
        args[7] = "1"; // tempo de estacionamento
        return new Driver(args, getNumParks());
    }

    private void launchAgents() {
        try{
            Park p = createNewPark1();
            pkspc.simpleAddPark(p);
            parkList.add(p);
            mainContainer.acceptNewAgent("Park 1" , p).start();
            Park p2 = createNewPark2();
            pkspc.simpleAddPark(p2);
            parkList.add(p2);
            mainContainer.acceptNewAgent("Park 2" , p2).start();

            Park p3 = createNewDynamicPark1();
            pkspc.simpleAddPark(p3);
            parkList.add(p3);
            mainContainer.acceptNewAgent("Park 3" , p3).start();
            Park p4 = createNewDynamicPark2();
            pkspc.simpleAddPark(p4);
            parkList.add(p4);
            mainContainer.acceptNewAgent("Park 4" , p4).start();
            Park p5 = createNewDynamicPark3();
            pkspc.simpleAddPark(p5);
            parkList.add(p5);
            mainContainer.acceptNewAgent("Park 5" , p5).start();
            Park p6 = createNewDynamicPark4();
            pkspc.simpleAddPark(p6);
            parkList.add(p6);
            mainContainer.acceptNewAgent("Park 6" , p6).start();

            for(int i = 0; i < getNumDrivers(); i++){
                Driver d = createNewDriver();
                d.setTimePark(i+1);
                pkspc.simpleAddDriver(d);
                driverList.add(d);
                mainContainer.acceptNewAgent("Driver " + i, d).start();
            }
            SimulationController sm = new SimulationController(getNumParks(),getNumDrivers(),300,10);
            mainContainer.acceptNewAgent("SimulationController 1", sm).start();
        } catch (StaleProxyException e){
            e.printStackTrace();
        }
    }
}
