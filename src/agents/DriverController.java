package agents;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import sajas.core.Agent;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.domain.DFService;


public class DriverController extends Agent{

    private ArrayList<Driver> drivers;

    class DriverControllerBehaviour extends SimpleBehaviour{
		private boolean end = false;

        public DriverControllerBehaviour(Agent a){
            super(a);
        }

        // TODO por aqui um timer para poder acabar a simulação ?
        // acho que isto ñ precisa de receber mensagens right?
        @Override
        public void action(){
           /* ACLMessage msg = blockingReceive();
            if(msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println(getLocalName() + ": recebi " + msg.getContent());
                if(msg.getContent().equals("info"))
                    sendInformation(msg);
                else rejectMessage(msg);
            }
            else if(msg.getPerformative() == ACLMessage.REQUEST){
                System.out.println("A Driver wants to park :O");
                if(msg.getContent().equals("park"))
                    startDrivers(msg);
            }
            else rejectMessage(msg);*/
        }

        private void startDrivers(ACLMessage msg){
            ACLMessage reply = msg.createReply();
            if(!(drivers.isEmpty())){
                reply.setPerformative(ACLMessage.REQUEST);
                reply.setContent("success");
                System.out.println("Started Drivers");
            }
            send(reply);
        }

        private void sendInformation(ACLMessage msg){
            // cria resposta
            ACLMessage reply = msg.createReply();
            // preenche conteudo da mensagem
            String answer = "Start Drivers";
            System.out.println("Sent " + answer);
            reply.setContent(answer);
            // envia mensagem
            send(reply);
        }
        private void rejectMessage(ACLMessage msg){
            ACLMessage reply = msg.createReply();
            msg.setPerformative(ACLMessage.UNKNOWN);
            System.err.println("Received message in DriverController with unexpected format");
            reply.setContent("unexpected format");
            send(reply);
        }

        public boolean done(){return end;}
    }


    protected void setup() {

        // regista agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente DriverController");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException e) {
            e.printStackTrace();
        }

        // cria behaviour
        DriverController.DriverControllerBehaviour b = new DriverController.DriverControllerBehaviour(this);
        addBehaviour(b);

        // pesquisa DF por agentes "driver" para poder começar a simulação
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Agente Driver");
        template.addServices(sd1);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for (DFAgentDescription aResult : result)
                msg.addReceiver(aResult.getName());
            msg.setContent("Start");
            send(msg);
            System.out.println("Started Drivers!");
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
