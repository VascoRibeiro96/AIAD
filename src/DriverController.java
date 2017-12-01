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


public class DriverController extends Agent{

    private ArrayList<Driver> drivers;
    class DriverControllerBehaviour extends SimpleBehaviour{
        private boolean end = false;

        public DriverControllerBehaviour(Agent a){
            super(a);
        }

        @Override
        public void action(){
        }


        private void rejectMessage(ACLMessage msg){
        }

        public boolean done(){return end;}
    }


    protected void setup() {

    }   // fim do metodo setup

    protected void takeDown(){
        try {
            DFService.deregister(this);
        } catch(FIPAException e){
            e.printStackTrace();
        }
    }
}
