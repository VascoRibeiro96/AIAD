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

    class SimulationControllerBehaviour extends SimpleBehaviour{
		private boolean end = false;
		private int driversOut = 0;
		private int curTick = 0;
		private final Object lockInc = new Object();

        private SimulationControllerBehaviour(Agent a){
            super(a);
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
                else rejectMessage(msg);
            }
            block();
        }

        private void updateDriverCount(){
            synchronized (lockInc) {
                driversOut++;
                if(driversOut == totalDrivers){
                    restartSimulation();
                }
            }
        }

        private void informParkEnd(){
            // TODO enviar mensagem para os parques fecharem
        }

        // TODO mandar para os parques também
        private void restartSimulation() {
            // pesquisa DF por agentes "driver" para poder recomeçar a simulação
            end = true;
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType("Agente Driver");
            template.addServices(sd1);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                for (DFAgentDescription aResult : result)
                    msg.addReceiver(aResult.getName());
                msg.setContent("Start");
                send(msg);
                System.out.println("Started Drivers!");
            } catch(FIPAException e) { e.printStackTrace(); }
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
    }   // fim do metodo setup


    protected void takeDown(){
        try {
            DFService.deregister(this);
        } catch(FIPAException e){
            e.printStackTrace();
        }
    }
}
