package agents;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import sajas.core.Agent;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.domain.DFService;


public class SimulationController extends Agent{

    private final int totalParks;
    private final int totalDrivers;
    private final int ticksPerSimulation;
    private final int numberSimulations;
    private int curSimulation = 0;

    class SimulationControllerBehaviour extends SimpleBehaviour{
		private boolean end = false;
		private int driversOut = 0;
		private int curTick = 0;
		private final Object lockInc = new Object();

        private SimulationControllerBehaviour(Agent a){
            super(a);
            curSimulation++;
        }

        @Override
        public void action() {
            curTick++;
            ACLMessage msg = myAgent.receive();
            if(curTick == ticksPerSimulation){ // should only happen once am i right?
                informParkEnd();
            }
            while (msg != null) {
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    if(msg.getContent().equals("driverOut")) {
                        updateDriverCount();
                    }
                    else rejectMessage(msg);
                }
                else if(msg.getPerformative() == ACLMessage.UNKNOWN){
                    System.err.println(msg.getContent());
                }
                else rejectMessage(msg);
                msg = myAgent.receive();
            }
            block();
        }

        private void updateDriverCount(){
            synchronized (lockInc) {
                driversOut++;
                if(driversOut == totalDrivers && curSimulation < numberSimulations){
                    restartSimulation();
                }
                else if (curSimulation >= numberSimulations) end = true;
            }
        }

        private void sendMessageTo(String agentType, String content){
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType(agentType);
            template.addServices(sd1);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                for (DFAgentDescription aResult : result)
                    msg.addReceiver(aResult.getName());
                msg.setContent(content);
                send(msg);
                System.out.println(getName() + " executed " + content);
            } catch(FIPAException e) { e.printStackTrace(); }
        }

        private void informParkEnd(){
            sendMessageTo("Agente Park", "Close");
        }

        private void restartSimulation() {
            // pesquisa DF por agentes "driver" para poder recomeçar a simulação
            end = true;
            sendMessageTo("Agente Driver", "Restart");
            sendMessageTo("Agente Park", "Restart");
            addBehaviour(new SimulationControllerBehaviour(myAgent));
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

    public SimulationController(int totalParks, int totalDrivers, int ticksPerSimulation, int numberSimulations){
        this.totalDrivers = totalDrivers;
        this.totalParks = totalParks;
        this.ticksPerSimulation = ticksPerSimulation;
        this.numberSimulations = numberSimulations;
    }

    protected void setup() {
        // regista agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName());
        sd.setType("Agente SimulationController");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
        // cria behaviour
        addBehaviour(new SimulationControllerBehaviour(this));

        System.out.println("This simulation is going to start! Nº of parks + " + totalParks + " and Nº of" + totalDrivers );
    }   // fim do metodo setup


    protected void takeDown(){
        try {
            DFService.deregister(this);
        } catch(FIPAException e){
            e.printStackTrace();
        }
    }
}
