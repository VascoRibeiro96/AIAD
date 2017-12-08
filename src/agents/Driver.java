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

import java.awt.*;
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
    private ParkInfo bestPark = null;
    private final int totalParks;

    private final Object lock1 = new Object();

    // cor dentro do carro = azul, fora = verde, a sair de cena = vermelho
    private Color curColor = Color.blue;


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

    class DriverQueryBehaviour extends SimpleBehaviour {
        private boolean end = false;

        private DriverQueryBehaviour(Agent a) {
            super(a);
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

        private DriverReceiveInfoBehaviour(Agent a){
            super(a);
        }

        @Override
        public void action (){
            ACLMessage msg = myAgent.receive();
            while (msg != null){
                if(msg.getPerformative() == ACLMessage.INFORM) {
                    System.out.println(getLocalName() + ": recebi " + msg.getContent());
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
            String name = values[1];
            double price = Double.parseDouble(values[2]);
            double x = Double.parseDouble(values[3]);
            double y = Double.parseDouble(values[4]);
            String type = values[5];
            ParkInfo p = new ParkInfo(name,type,price,x,y);
            synchronized (lock1){ // evitar problemas de concurrencia. Funciona como um lock
                if(bestPark == null || bestPark.utility < p.getUtility(utility,timePark,dest)){
                    bestPark = p;
                    System.out.println("New util best for " + name + "! " + bestPark.getUtility(utility,timePark,dest));
                }
                parkUtilities.add(p);
                msgReceived++;
                if(msgReceived == totalParks) {
                    end = true;
                    addBehaviour(new DriverTravelParkBehaviour(myAgent,bestPark));
                    addBehaviour(new DriverParkResponseBehaviour(myAgent));
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
                parkRequest(ACLMessage.REQUEST,"park", finalPark);
                end = true;
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
            curColor = Color.red;
        }

        @Override
        public void action() {
            Coords cur = new Coords(curX, curY);
            Coords des = new Coords(0,0);
            if(cur.calculateDistance(des) != 0){
                updateCurCoords(cur,des);
            }
            else {
                end = true;
            }
        }

        @Override
        public boolean done() {
            return end;
        }
    }

    class DriverParkResponseBehaviour extends SimpleBehaviour{
        private boolean end = false;
        private ParkInfo park2park = bestPark;
        private ArrayList<String> parksFull = new ArrayList<>();

        private DriverParkResponseBehaviour(Agent a){
            super(a);
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

        private void updatePark2Park(){
            parksFull.add(park2park.name);
            ParkInfo goodPark = null;
            for(ParkInfo park : parkUtilities){
                if(parksFull.contains(park.name))continue;
                if(park.utility <= 0){
                    parksFull.add(park.name);
                    continue;
                }
                if(goodPark == null || goodPark.utility < park.getUtility(utility,timePark,dest))
                    goodPark = park;
            }
            if(goodPark == null) return;
            park2park = goodPark;
        }

        private void parkDriver(){
            addBehaviour(new DriverWalkToDestBehaviour(myAgent,park2park));
            curColor = Color.green;
            end = true;
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
                if(curTime >= timePark){
                    if(cur.calculateDistance(parkLocation) != 0){
                        updateCurCoords(cur,parkLocation);
                    }
                    else{
                        end = true;
                        parkRequest(ACLMessage.INFORM,"leave", park2park);
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
    	/*
        Object[] args = getArguments();
        if(args != null && args.length == 8) {
            initVariables(args);
        } else {
            System.err.println("Missing Parameters!");
            return;
        }

        if(type.equals("") || !(type.equals("explorer") || type.equals("rational"))){
            System.err.println("Introduced wrong type for driver!");
            System.err.println("Typed introduced: " + type);
            return;
        }
		*/
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

        // cria behaviour
        //Driver.DriverBehaviour b = new Driver.DriverBehaviour(this);
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
        simGraphics.drawRoundRect(curColor);
    }

    @Override
    public int getX() {
        return curX;
    }

    @Override
    public int getY() {
        return curY;
    }

    public void stepi(){
        curX ++;
        curY --;
    }
}
