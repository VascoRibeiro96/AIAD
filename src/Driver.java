import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class Driver extends Agent {

    class DriverBehaviour extends SimpleBehaviour{
        private boolean end = false;

        public DriverBehaviour(Agent a){
            super(a);
        }

        @Override
        public void action(){

        }

        public boolean done(){return end;}
    }

    protected void setup() {

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


        // pesquisa DF por agentes "park"
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Agente park");
        template.addServices(sd1);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            // envia mensagem "drive" inicial a todos os agentes "park"
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for(int i=0; i<result.length; ++i)
                msg.addReceiver(result[i].getName());
            msg.setContent("driver");
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
