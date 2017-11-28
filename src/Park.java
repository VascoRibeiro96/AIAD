import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import java.util.ArrayList;

public class Park extends Agent {
    private String name;
    private double price;
    private int spots;
    private String type;
    private ArrayList<Integer> spotStory;
    //TODO something else right

    //ter logica do comportamento aqui?
    class ParkBehaviour extends SimpleBehaviour{
        private boolean end = false;

        public ParkBehaviour(Agent a){
            super(a);
        }


        //TODO receber uma mensagem
        @Override
        public void action() {
            ACLMessage msg = blockingReceive();
            if(msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println(getLocalName() + ": recebi " + msg.getContent());
                if(msg.getContent().equals("info"))
                    sendInformation(msg);

            }
            else if(msg.getPerformative() == ACLMessage.PROPOSE){
                System.out.println("A Driver wants to park :O");
                //TODO precisa de check?
                updateParking(msg);
            }
        }

        private void updateParking(ACLMessage msg){
            ACLMessage reply = msg.createReply();
            if(spots != 0){
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                reply.setContent("success");
                spots--;
                System.out.println("Oh yee parked! Current space " + spots);
            }
            else{
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                reply.setContent("no:(");
                System.out.println("Park full!");
                end = true;
            }
            send(reply);
        }

        private void sendInformation(ACLMessage msg){
            // cria resposta
            ACLMessage reply = msg.createReply();
            // preenche conteudo da mensagem
            String answer = name + " " + price + " " + spots;
            System.out.println("Sent " + answer);
            reply.setContent(answer);
            // envia mensagem
            send(reply);
        }

        @Override
        public boolean done() {
            return end;
        }
    }

    //tipos de parque = static & dynamic
    //tem de ter 2 args tipo e nome (falta dp checkar para ñ haver nomes iguais)
    protected void setup() {
        String type = "";
        Object[] args = getArguments();
        if(args != null && args.length > 0) {
            type = (String) args[0];
            name = "lol";//(String) args[1];
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

        initVars();

        // cria behaviour
        ParkBehaviour b = new ParkBehaviour(this);
        addBehaviour(b);


    }   // fim do metodo setup

    private void initVars(){
        price = 1;
        spots = 20;
        spotStory = new ArrayList<>();
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
