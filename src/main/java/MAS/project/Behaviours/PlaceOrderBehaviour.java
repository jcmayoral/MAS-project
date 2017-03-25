package Behaviors;

import java.io.IOException;
import java.util.Random;

import Messages.CustomerOrder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.util.Vector;

public class PlaceOrderBehaviour extends TickerBehaviour {

  protected Vector<AID> FactoryAgents = new Vector<AID>();

  public PlaceOrderBehaviour(Agent a, long period) {
    super(a, period);
    // TODO Auto-generated constructor stub
  }

  public void setFactoryAID(Vector<AID> factoriesid){
    FactoryAgents  = factoriesid;
  }

  @Override
  protected void onTick() {
    // TODO Auto-generated method stub
    if (FactoryAgents.size() > 0) {
      CustomerOrder order = getOrder();
      try {
        // place order to factory agents
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(FactoryAgents.elementAt(0));
        //System.out.println("sending to " + FactoryAgents.elementAt(0).getLocalName());
        msg.setLanguage("English");
        msg.setContentObject(order);
        myAgent.send(msg);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        int High = 3;

        e.printStackTrace();
      }
    }
  }

  public CustomerOrder getOrder() {
    String type = null;
    Random r = new Random();
    int Low = 1;
    int High = 3;
    int orderType = r.nextInt(High - Low) + Low;
    int numberPieces = r.nextInt(10);

    switch (orderType) {

    case 1: {
      type = "Bearing";
    }
      break;
    case 2: {
      type = "Bearing-box";
    }
      break;
    default:
      break;
    }

    return (new CustomerOrder(this.myAgent.getAID(), type, numberPieces));
  }
}
