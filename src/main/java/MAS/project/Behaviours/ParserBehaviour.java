package Behaviours;
import java.io.IOException;
import java.util.Random;
import Messages.CustomerOrder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;

public class ParserBehaviour extends TickerBehaviour {

  public String fileName;
  private boolean firstCycle = true;
  protected Vector<AID> FactoryAgents = new Vector<AID>();
  private Vector<CustomerOrder> orders = new Vector<CustomerOrder>();

  public ParserBehaviour(Agent a, long period) {
    super(a, period);
    // TODO Auto-generated constructor stub
  }

  public void updateParams(String filename){
    fileName = filename;
    System.out.println("File: " + fileName);
  }

  public void setFactoryAID(Vector<AID> factoriesid){
    FactoryAgents  = factoriesid;
  }

  @Override
  protected void onTick() {

    if (firstCycle){
        readOrders();
        firstCycle = false;
    }
    else{

      if (FactoryAgents.size() > 0) {
        CustomerOrder order = getOrder();

        if (order != null){
          try {
            // place order to factory agents
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(FactoryAgents.elementAt(0));
            //System.out.println("sending" + order.getCustomerAID()+ "to " + FactoryAgents.elementAt(0).getLocalName());
            msg.setLanguage("English");
            msg.setContentObject(order);
            myAgent.send(msg);
          }
          catch (IOException e) {
          // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        else{
          System.out.println(myAgent.getLocalName() + ": No more orders");
          myAgent.doDelete();
        }
      }
    }

  }

  public CustomerOrder getOrder() {

    if(orders.isEmpty()){
      //System.out.println("IS EMPTY");
      return null;
    }
    CustomerOrder nextOrder = new CustomerOrder(orders.firstElement());
    orders.remove(0);
    return (nextOrder);
  }

  protected void readOrders(){

    System.out.println("Trying to read orders from file path" + fileName);
    BufferedReader reader = null;
    CustomerOrder tmp;
    String sCurrentLine;
    String[] parser;

    try{

      reader = new BufferedReader(new FileReader(fileName));

      while((sCurrentLine = reader.readLine())!= null){
        parser = sCurrentLine.split(" ");
        orders.add(new CustomerOrder(parser));

      }
    }
    catch (IOException e){
        System.out.println("EXCEPTION");
        e.printStackTrace();
    }
  }

}
