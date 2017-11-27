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

    protected void setup(){

    }

    protected void takeDown(){
        try {
            DFService.deregister(this);
        } catch(FIPAException e){
            e.printStackTree();
        }
    }
}
