import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import utils.Coords;

import java.util.ArrayList;

public class Park extends Agent {
    private String name;
    private double price;
    private int spots;
    private String type;
    private ArrayList<Integer> spotStory;
    private Coords location;
    //TODO something else right talvez horario de abertura e fecho?

    //ter logica do comportamento aqui?
    class ParkBehaviour extends SimpleBehaviour{
        private boolean end = false;
        private final Object lock2 = new Object();

        private ParkBehaviour(Agent a){
            super(a);
        }

        @Override
        public void action() {
            ACLMessage msg = blockingReceive();
            if(msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println(getLocalName() + ": recebi " + msg.getContent());
                if(msg.getContent().equals("info"))
                    sendInformation(msg);
                else if(msg.getContent().equals("leave"))
                    freeSpot();
                else rejectMessage(msg);
            }
            else if(msg.getPerformative() == ACLMessage.REQUEST){
                System.out.println("A Driver wants to park :O");
                if(msg.getContent().equals("park"))
                    updateParking(msg);
            }
            else rejectMessage(msg);
        }

        private void updateParking(ACLMessage msg){
            ACLMessage reply = msg.createReply();
            synchronized (lock2) {
                if(spots != 0){
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("success");
                    spots--;
                    System.out.println("Oh yee parked! Current space " + spots);
                }
                else{
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("unavailable");
                    System.out.println("Park full!");
                }
                send(reply);
            }
        }

        private void freeSpot(){
            synchronized (lock2) {
                spots++;
            }
        }

        private void sendInformation(ACLMessage msg){
            // cria resposta
            ACLMessage reply = msg.createReply();
            // preenche conteudo da mensagem
            String answer = "retInfo," + name + "," + price + "," + location + "," + type;
            System.out.println("Sent " + answer);
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
    protected void setup() {
        Object[] args = getArguments();
        if(args != null && args.length == 5) {
            type = (String) args[0];
            name = getName();
            price = Double.parseDouble((String) args[1]);
            spots = Integer.parseInt((String) args[2]);
            double x = Double.parseDouble((String) args[3]);
            double y = Double.parseDouble((String) args[4]);
            location = new Coords(x,y);
            spotStory = new ArrayList<>();

        } else {
            System.err.println("Missing Parameters!");
            return;
        }
        if(type.equals("") || !(type.equals("static") || type.equals("dynamic"))){
            System.err.println("Introduced wrong type for park!");
            System.err.println("Typed introduced: " + type);
            return;
        }

        // regista agente no DF
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

    //metodo takeDown
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }
}
