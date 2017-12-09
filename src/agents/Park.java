package agents;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;

import agents.utils.Coords;
import sajas.core.Agent;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.domain.DFService;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;
import java.util.ArrayList;


public class Park extends Agent implements Drawable {
    private String name;
    private double price;
    private int totalSpots;
	private int spots; // nº de lugares vagos
    private String type;
    private ArrayList<Double> revenueStory;
    private Coords location;

    //ter logica do comportamento aqui?
    class ParkBehaviour extends SimpleBehaviour{
		// private static final long serialVersionUID = 1L;
		private boolean end = false;
        private final Object lock2 = new Object();
        private double totalRevenue = 0;
        private boolean close = false;

        private ParkBehaviour(Agent a){
            super(a);
            System.out.println(name + " opened!");
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            while (msg != null){
                if(msg.getPerformative() == ACLMessage.INFORM) {
                    // System.out.println(getLocalName() + ": recebi " + msg.getContent());
                    if(msg.getContent().equals("info"))
                        sendInformation(msg);
                    else if(msg.getContent().contains("leave,"))
                        freeSpot(msg);
                    else if (msg.getContent().equals("Close")){
                        close = true;
                    }
                    else if (msg.getContent().equals("RestartPark")){
                        end = true;
                        revenueStory.add(totalRevenue);
                        spots = totalSpots;
                        addBehaviour(new ParkBehaviour(myAgent));
                    }
                    else rejectMessage(msg);
                }
                else if(msg.getPerformative() == ACLMessage.REQUEST){
                    System.out.println("A Driver wants to park!");
                    if(msg.getContent().equals("park"))
                        updateParking(msg);
                }
                else if(msg.getPerformative() == ACLMessage.UNKNOWN){
                    System.err.println(msg.getContent());
                }
                else rejectMessage(msg);
                msg = myAgent.receive();
            }
            if (msg == null){
                block();
            }
        }

        private void updateParking(ACLMessage msg){
            ACLMessage reply = msg.createReply();
            synchronized (lock2) {
                if(spots != 0 && !close){
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("success");
                    spots--;
                    System.out.println("A driver parked! Current space " + spots);
                }
                else{
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("unavailable");
                    if(!close)System.out.println(name + " is full!");
                    else System.out.println("Park is closed!");
                }
                send(reply);
            }
        }

        private void freeSpot(ACLMessage msg){
            synchronized (lock2) {
                spots++;
                String[] splitMsg = msg.getContent().split(",");
                totalRevenue += Double.parseDouble(splitMsg[1]) * price;
                System.out.println(name + " total revenue of the day increased to " + totalRevenue);
            }
        }

        private void sendInformation(ACLMessage msg){
            // cria resposta
            ACLMessage reply = msg.createReply();
            // preenche conteudo da mensagem
            String answer = "retInfo," + name + "," + price + "," + location + "," + type;
            reply.setContent(answer);
            // envia mensagem
            send(reply);
        }

        // mensagem default caso receba uma mensagem inesperada
        private void rejectMessage(ACLMessage msg){
            ACLMessage reply = msg.createReply();
            msg.setPerformative(ACLMessage.UNKNOWN);
            System.err.println("Received message with unexpected format");
            reply.setContent("unexpected format");
            send(reply);
        }

        @Override
        public boolean done() {
            return end;
        }
    }

    // tem de ter 5 argumentos por esta ordem: tipo (static ou dynamic), preço, nº lugares, Longitude(ou x), Latitude(ou y)
    // exemplo static, 10, 3, 49.3, 54.1

    private void initVariables(Object[] args){
    	type = (String) args[0];
        price = Double.parseDouble((String) args[1]);
        spots = Integer.parseInt((String) args[2]);
        totalSpots = spots;
        double x = Double.parseDouble((String) args[3]);
        double y = Double.parseDouble((String) args[4]);
        location = new Coords(x,y);
        revenueStory = new ArrayList<>();
    }
    
    public Park(Object[] args){
    	initVariables(args);
    }
    

    protected void setup() {
        // regista agente no DF
    	name = getName();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente Park");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException e) {
            e.printStackTrace();
        }

        getVars();

        // cria behaviour
        ParkBehaviour b = new ParkBehaviour(this);
        addBehaviour(b);
    }   // fim do metodo setup

    private void getVars(){
        System.out.println("New park created: " + name + " " + price + " " + spots + " " + location);
    }

    public int getX(){
        return (int)location.x;
    }

    public int getY(){
        return (int)location.y;
    }

    @Override
    public void draw(SimGraphics simGraphics) {
        simGraphics.drawRoundRect(Color.yellow);
    }

    public void updateLocation(int x, int y){
        location = new Coords(x,y);
    }

    //metodo takeDown
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

	public String getParkName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getSpots() {
		return spots;
	}

	public void setSpots(int spots) {
		this.spots = spots;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Coords getLocation() {
		return location;
	}

	public void setLocation(Coords location) {
		this.location = location;
	}
}
