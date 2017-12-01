import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import utils.Coords;
import utils.ParkInfo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Driver extends Agent {
    private Coords start, dest;
    private String name;
    private String type;
    // tem tempo previsto de chegada mas ñ sei para que serve
    private double maxMoney; // max money to pay per hour
    private double maxDist; //|parkLocation-maxDist|
    private double timePark, timeArrive; // acho que o timearrive ñ serve para nada
    private double utility; // isto é o Ci (maxMoney*timePark+maxDist)
    private ArrayList<ParkInfo> parkUtilities; // Ci - (alpha * price * time) - (beta - distance)
    private ParkInfo bestPark = null;
    private Object lock1 = new Object();
    // TODO dp por variávies aleatórias para isto aqui em cima

    class DriverBehaviour extends SimpleBehaviour{
        private boolean end = false;

        public DriverBehaviour(Agent a){
            super(a);
        }

        @Override
        public void action(){
            ACLMessage msg = blockingReceive();
            if(msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println(getLocalName() + ": recebi " + msg.getContent());
                if(msg.getContent().contains("retInfo,"))
                    updateParkUtils(msg);
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
        name = getName();
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

    protected void setup() {
        Object[] args = getArguments();
        // args: tipo de driver(explorer, rational), xi, yi, xf, yf, maxMoney, maxDist, timePark
        // exemplo:explorer, 49.3, 49.4, 65.12, 12.2, 25, 100, 2
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

        // regista agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente driver");
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

    }   // fim do metodo setup

    protected void takeDown(){
        try {
            DFService.deregister(this);
        } catch(FIPAException e){
            e.printStackTrace();
        }
    }
}
