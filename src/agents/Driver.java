package agents;

import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;

import java.awt.*;
import java.util.ArrayList;

import agents.utils.Coords;
import agents.utils.ParkInfo;
import sajas.core.Agent;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.domain.DFService;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

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
    private double timePark, timeArrive; // acho que o timearrive ñ serve para nada
    private double utility; // isto é o Ci (maxMoney*timePark+maxDist)
    private ArrayList<ParkInfo> parkUtilities; // Ci - (alpha * price * time) - (beta - distance)
    private ParkInfo bestPark = null;
    private final Object lock1 = new Object();

    // TODO dp por variávies aleatórias para isto aqui em cima

    class DriverBehaviour extends SimpleBehaviour{
        private boolean end = false;
        private ArrayList<String> parksFull = new ArrayList<>();
        private boolean hasParked = false;
        private ParkInfo park2park; // parque para estacionar (pqp ingles é tudo a mesma coisa)

        private DriverBehaviour(Agent a){
            super(a); // o nome desta variável é myAgent
        }

        @Override
        public void action(){
            ACLMessage msg = blockingReceive();
            if(msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println(getLocalName() + ": recebi " + msg.getContent());
                if(msg.getContent().contains("retInfo,"))
                    updateParkUtils(msg);
                else if(msg.getContent().equals("Start")){
                    park2park = bestPark;
                    parkRequest(ACLMessage.REQUEST,"park");
                }
                else rejectMessage(msg);
            }
            else if(msg.getPerformative() == ACLMessage.AGREE) {
                if(msg.getContent().equals("success"))
                    parkDriver();
            }
            else if(msg.getPerformative() == ACLMessage.REFUSE) {
                if(msg.getContent().equals("unavailable")){
                    updatePark2Park();
                    if(parksFull.size() != parkUtilities.size())
                        parkRequest(ACLMessage.REQUEST,"park");
                }
                else rejectMessage(msg);
            }
            else rejectMessage(msg);
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
                    System.out.println("New util best! " + bestPark.getUtility(utility,timePark,dest));
                }
                parkUtilities.add(p);
            }
        }

        private void updatePark2Park(){
            parksFull.add(park2park.name);
            ParkInfo goodPark = null;
            for(ParkInfo park : parkUtilities){
                if(parksFull.contains(park.name))continue;
                if(goodPark == null || goodPark.utility < park.getUtility(utility,timePark,dest))
                    goodPark = park;
            }
            if(goodPark == null) return;
            park2park = goodPark;
        }

        // envia mensagem para o parque selecionado para estacionar
        // 1º tipo de mensagem é perguntar ao melhor parque calculado se pode lá estacionar.
        // 2ª tipo de mensagem é informar o parque onde estacionou que pretende sair.
        // request é o tipo de mensagem (inform ou request) e content é o conteúdo da mensagem
        private void parkRequest(final int request, String content){
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType("Agente Park");
            template.addServices(sd1);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                ACLMessage msg = new ACLMessage(request);
                for (DFAgentDescription aResult : result)
                    if (aResult.getName().getName().equals(park2park.name))
                        msg.addReceiver(aResult.getName());
                msg.setContent(content);
                send(msg);
            } catch(FIPAException e) { e.printStackTrace(); }
        }

        private void parkDriver(){
            hasParked = true;
            new Thread(() -> {
                try {
                    long time = (long)(timePark * 60 * 60 * 1000); // timePark = horas logo isto converte para ms
                    sleep(time);
                    System.out.println("Driver " + name + " parking ended!");
                    parkRequest(ACLMessage.INFORM,"leave");
                } catch (InterruptedException e) {
                    System.err.println("Parking on" + name + " was stopped! " + e.getMessage());
                }
            }).start();
        }

        private void rejectMessage(ACLMessage msg){
            ACLMessage reply = msg.createReply();
            msg.setPerformative(ACLMessage.UNKNOWN);
            System.err.println("Driver received message with unexpected format");
            reply.setContent("unexpected format");
            send(reply);
        }

        public boolean done(){return end;}
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
    public Driver(Object[] args){
    	initVariables(args);
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
        Driver.DriverBehaviour b = new Driver.DriverBehaviour(this);
        addBehaviour(b);

        System.out.println("Driver " + name + " was created! Total util:" + utility);
        queryParks();
    }   // fim do metodo setup

    private void queryParks(){
        // pesquisa DF por agentes "park"
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Agente Park");
        template.addServices(sd1);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            // envia mensagem "drive" inicial a todos os agentes "park"
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for(int i=0; i<result.length; ++i)
                msg.addReceiver(result[i].getName());
            msg.setContent("info");
            send(msg);
        } catch(FIPAException e) { e.printStackTrace(); }
    }

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

    @Override
    public void draw(SimGraphics simGraphics) {
        //simGraphics.drawFastRoundRect(Color.blue);
        simGraphics.draw4ColorHollowRect(Color.blue,Color.green,Color.red,Color.yellow);
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
    	System.out.println("Driver " + name + " cenas " + start);
    	curX ++;
    	curY --;
    }
}
