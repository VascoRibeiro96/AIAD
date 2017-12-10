package agents;

import agents.utils.Coords;
import agents.utils.ParkInfo;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import sajas.core.Agent;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.domain.DFService;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Driver extends Agent implements Drawable {
    private Coords start, dest;
    private int curX;
    private int curY;
    private String name;
    private String type;
    // tem tempo previsto de chegada mas ñ sei para que serve
    private double maxMoney; // max money to pay per hour
    private double maxDist; //|parkLocation-maxDist|
    private double timePark, timeArrive; // em horas (1 tick = 1 h)
    private double utility; // isto é o Ci (maxMoney*timePark+maxDist)


    private ArrayList<ParkInfo> parkUtilities; // Ci - (alpha * price * time) - (beta - distance)
    private ParkInfo park2park;
    private ArrayList<String> parksFull = new ArrayList<>();
    private ParkInfo bestPark = null;
    private final int totalParks;

    // cor dentro do carro = azul, fora = verde, a sair de cena = vermelho
    private  File curimageFile = new File("icons/bluecar.png");


    // TODO dp por variávies aleatórias para isto aqui em cima


    // common action to reject unknown messages (more of a debugg func)
    private void rejectMessage(ACLMessage msg){
        ACLMessage reply = msg.createReply();
        msg.setPerformative(ACLMessage.UNKNOWN);
        System.err.println("Driver received message with unexpected format");
        reply.setContent("unexpected format");
        send(reply);
    }

    // envia mensagem para o parque selecionado para estacionar
    // 1º tipo de mensagem é perguntar ao melhor parque calculado se pode lá estacionar.
    // 2ª tipo de mensagem é informar o parque onde estacionou que pretende sair.
    // request é o tipo de mensagem (inform ou request) e content é o conteúdo da mensagem
    private void parkRequest(final int request, String content, ParkInfo park2park){
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Agente Park");
        template.addServices(sd1);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            ACLMessage msg = new ACLMessage(request);
            for (DFAgentDescription aResult : result)
                if (aResult.getName().getName().equals(park2park.name))
                    msg.addReceiver(aResult.getName());
            msg.setContent(content);
            send(msg);
        } catch(FIPAException e) { e.printStackTrace(); }
    }

    private void updateCurCoords(Coords cur, Coords des){
        if(cur.x < des.x){
            curX++;
        }
        if(cur.x > des.x){
            curX--;
        }
        if(cur.y < des.y){
            curY++;
        }
        if(cur.y > des.y){
            curY--;
        }
    }

    private void updatePark2Park(){
        parksFull.add(park2park.name);
        ParkInfo goodPark = null;
        for(ParkInfo park : parkUtilities){
            if(parksFull.contains(park.name))continue;
            if(type.equals("rational")){
                if(park.utility <= 0){
                    parksFull.add(park.name);
                    continue;
                }
                if(goodPark == null || goodPark.utility < park.getUtility(utility,timePark,dest))
                    goodPark = park;
            }
            else{
                park.distanceTo = park.location.calculateDistance(new Coords(curX,curY));
                if(goodPark == null || goodPark.distanceTo > park.distanceTo)
                    goodPark = park;
            }
        }
        if(goodPark == null) return;
        park2park = goodPark;
    }

    class DriverQueryBehaviour extends SimpleBehaviour {
        private boolean end = false;

        private DriverQueryBehaviour(Agent a) {
            super(a);
            parksFull = new ArrayList<>();
        }

        @Override
        public void action () {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType("Agente Park");
            template.addServices(sd1);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                // envia mensagem inicial a todos os agentes "park"
                if (result.length == totalParks){
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    for (DFAgentDescription aResult : result)
                        msg.addReceiver(aResult.getName());
                    msg.setContent("info");
                    send(msg);
                    end = true;
                }
            } catch(FIPAException e) { e.printStackTrace(); }
        }

        @Override
        public boolean done () {
            return end;
        }
    }

    class DriverReceiveInfoBehaviour extends SimpleBehaviour {
        private boolean end = false;
        private int msgReceived = 0;
        private final Object lock1 = new Object();

        private DriverReceiveInfoBehaviour(Agent a){
            super(a);
        }

        @Override
        public void action (){
            ACLMessage msg = myAgent.receive();
            while (msg != null){
                if(msg.getPerformative() == ACLMessage.INFORM) {
                    if (msg.getContent().contains("retInfo,"))
                        updateParkUtils(msg);
                    else rejectMessage(msg);
                }
                else rejectMessage(msg);
                msg = myAgent.receive();
            }
            block();
        }

        private void updateParkUtils(ACLMessage msg){
            String[] values = msg.getContent().split(",");
            String Pname = values[1];
            double price = Double.parseDouble(values[2]);
            double x = Double.parseDouble(values[3]);
            double y = Double.parseDouble(values[4]);
            String typeT = values[5];
            double hourInflation = Double.parseDouble(values[6]);
            ParkInfo p = new ParkInfo(Pname,typeT,price,x,y,hourInflation);
            synchronized (lock1){ // evitar problemas de concurrencia. Funciona como um lock
                if(type.equals("rational")){
                    if(bestPark == null || bestPark.utility < p.getUtility(utility,timePark,dest)){
                        bestPark = p;
                    }
                }
                else{
                    p.getUtility(utility,timePark,dest);
                    p.distanceTo = p.location.calculateDistance(start);
                    if(bestPark == null || bestPark.distanceTo > p.distanceTo){
                        bestPark = p;
                    }
                }
                parkUtilities.add(p);
                msgReceived++;
                if(msgReceived == totalParks) {
                    end = true;
                    if(type.equals("rational"))System.out.println("New util best for " + name + "! " + bestPark.getUtility(utility,timePark,dest));
                    if(bestPark.utility < 0 && type.equals("rational")){
                        addBehaviour(new DriverExitSceneBehaviour(myAgent));
                    }
                    else {
                        addBehaviour(new DriverTravelParkBehaviour(myAgent,bestPark));
                        addBehaviour(new DriverParkResponseBehaviour(myAgent));
                    }
                }
            }
        }

        @Override
        public boolean done(){
            return end;
        }
    }

    class DriverTravelParkBehaviour extends SimpleBehaviour{
        private boolean end = false;
        private ParkInfo finalPark;

        private DriverTravelParkBehaviour(Agent a, ParkInfo p){
            super(a);
            finalPark = p;
        }

        @Override
        public void action() {
            Coords cur = new Coords(curX, curY);
            Coords des = finalPark.location;
            if(cur.calculateDistance(des) != 0){
                updateCurCoords(cur,des);
            }
            else {
                if(type.equals("explorer") && finalPark.utility < 0)  {
                    updatePark2Park();
                    finalPark = park2park;
                }
                else {
                    parkRequest(ACLMessage.REQUEST,"park", finalPark);
                    end = true;
                }
            }
        }

        @Override
        public boolean done() {
            return end;
        }
    }

    class DriverExitSceneBehaviour extends SimpleBehaviour{
        private boolean end = false;

        private DriverExitSceneBehaviour(Agent a){
            super(a);
            //Vermelho
            curimageFile = new File("icons/redcar.png");
        }

        @Override
        public void action() {
            Coords cur = new Coords(curX, curY);
            if(cur.calculateDistance(start) != 0){
                updateCurCoords(cur,start);
            }
            else {
                end = true; // driverOut
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd1 = new ServiceDescription();
                sd1.setType("Agente SimulationController");
                template.addServices(sd1);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    for (DFAgentDescription aResult : result)
                        msg.addReceiver(aResult.getName());
                    msg.setContent("driverOut");
                    send(msg);
                } catch(FIPAException e) { e.printStackTrace(); }
                addBehaviour(new DriverWaitRestartBehaviour(myAgent));
            }
        }

        @Override
        public boolean done() {
            return end;
        }
    }

    class DriverParkResponseBehaviour extends SimpleBehaviour{
        private boolean end = false;

        private DriverParkResponseBehaviour(Agent a){
            super(a);
            park2park = bestPark;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            while (msg != null){
                if(msg.getPerformative() == ACLMessage.AGREE) {
                    if(msg.getContent().equals("success"))
                        parkDriver();
                    else rejectMessage(msg);
                }
                else if(msg.getPerformative() == ACLMessage.REFUSE) {
                    if(msg.getContent().equals("unavailable")){
                        updatePark2Park();
                        if(parksFull.size() != parkUtilities.size())
                             addBehaviour(new DriverTravelParkBehaviour(myAgent,park2park));
                        else {
                            end = true;
                            addBehaviour(new DriverExitSceneBehaviour(myAgent));
                        }
                    }
                    else rejectMessage(msg);
                }
                else rejectMessage(msg);
                msg = myAgent.receive();
            }
            block();
        }

        private void parkDriver(){
            if(type.equals("explorer") && park2park.utility < 0){
                updatePark2Park();
                if(parksFull.size() != parkUtilities.size())
                    addBehaviour(new DriverTravelParkBehaviour(myAgent,park2park));
                else {
                    end = true;
                    addBehaviour(new DriverExitSceneBehaviour(myAgent));
                }
            }
            else {
                addBehaviour(new DriverWalkToDestBehaviour(myAgent,park2park));
                //verde
                curimageFile = new File("icons/greencar.png");
                end = true;
            }
        }

        @Override
        public boolean done() {
            return end;
        }
    }

    class DriverWalkToDestBehaviour extends SimpleBehaviour{
        private boolean end = false;
        private ParkInfo park2park;
        private Coords parkLocation;
        private boolean comeback = false;
        private int curTime = 0;

        private DriverWalkToDestBehaviour(Agent a, ParkInfo park2park){
            super(a);
            this.park2park = park2park;
            parkLocation = park2park.location;
        }

        @Override
        public void action() {
            Coords cur = new Coords(curX, curY);
            if(!comeback && cur.calculateDistance(dest) != 0){
                updateCurCoords(cur,dest);
            }
            else {
                comeback = true;
                curTime++;
                if(curTime >= timePark*50){
                    if(cur.calculateDistance(parkLocation) != 0){
                        updateCurCoords(cur,parkLocation);
                    }
                    else{
                        end = true;
                        String msgContent = "leave," + timePark;
                        parkRequest(ACLMessage.INFORM,msgContent, park2park);
                        addBehaviour(new DriverExitSceneBehaviour(myAgent));
                    }
                }
            }
        }

        @Override
        public boolean done() {
            return end;
        }
    }

    class DriverWaitRestartBehaviour extends SimpleBehaviour{
        private boolean end = false;

        private DriverWaitRestartBehaviour(Agent a){
            super(a);
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            while (msg != null){
                if(msg.getPerformative() == ACLMessage.INFORM) {
                    if (msg.getContent().contains("Restart")){
                        end = true;
                        //Azul
                        curimageFile = new File("icons/bluecar.png");
                        bestPark = null; // se calhar isto até nem era preciso
                        parkUtilities = new ArrayList<>();
                        park2park = null;
                        parksFull = new ArrayList<>();
                        addBehaviour(new DriverQueryBehaviour(myAgent));
                        addBehaviour(new DriverReceiveInfoBehaviour(myAgent));
                    }
                    else rejectMessage(msg);
                }
                else rejectMessage(msg);
                msg = myAgent.receive();
            }
            block();
        }

        @Override
        public boolean done() {
            return end;
        }
    }

    private void initVariables(Object[] args){
        type = (String) args[0];
        double xi =Double.parseDouble((String) args[1]);
        double yi =Double.parseDouble((String) args[2]);
        double xf =Double.parseDouble((String) args[3]);
        double yf =Double.parseDouble((String) args[4]);
        start = new Coords(xi,yi);
        dest = new Coords(xf,yf);
        maxMoney =Double.parseDouble((String) args[5]);
        maxDist =Double.parseDouble((String) args[6]);
        timePark =Double.parseDouble((String) args[7]);
        utility = timePark * maxMoney + maxDist;
        parkUtilities = new ArrayList<>();
    }

    // args: tipo de driver(explorer, rational), xi, yi, xf, yf, maxMoney, maxDist, timePark
    // exemplo:explorer, 49.3, 49.4, 65.12, 12.2, 25, 100, 2
    public Driver(Object[] args, int totalParks){
        initVariables(args);
        this.totalParks = totalParks;
    }

    protected void setup() {
        // regista agente no DF
        name = getName();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente Driver");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException e) {
            e.printStackTrace();
        }

        // addBehaviour(b);
        addBehaviour(new DriverQueryBehaviour(this));
        addBehaviour(new DriverReceiveInfoBehaviour(this));
        System.out.println("Driver " + name + " was created! Total util:" + utility);
    }   // fim do metodo setup

    protected void takeDown(){
        try {
            DFService.deregister(this);
        } catch(FIPAException e){
            e.printStackTrace();
        }
    }

    public Coords getStart(){
        return start;
    }

    public void setStart(double x, double y){
        start = new Coords(x,y);
        curX = (int) start.x;
        curY = (int) start.y;
    }

    public void setDest(double x, double y){
        dest = new Coords(x,y);
    }

    @Override
    public void draw(SimGraphics simGraphics) {
        BufferedImage image = null;
        try {
           image = ImageIO.read(curimageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        simGraphics.drawImageScaled(image);
    }

    @Override
    public int getX() {
        return curX;
    }

    @Override
    public int getY() {
        return curY;
    }

    public void setTimePark(double t){
        timePark = t;
        utility = timePark * maxMoney + maxDist;
    }
}
