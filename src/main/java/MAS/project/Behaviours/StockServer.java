package Behaviours;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import Messages.CustomerOrder;

public class StockServer extends CyclicBehaviour {

  private int currentStock = 0;
  private int initialStock = 0;
  private boolean isAssigned = false;

  public StockServer(){
    //System.out.println(getClass() + " : Empty Constructor");
  }

  public void setStock(int i){
    currentStock = i;
    initialStock = i;
  }

  private void checkStock(){
    if (currentStock <= 6){
      System.out.println("Refilling " + myAgent.getLocalName());
      /*
      myAgent.doSuspend();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      myAgent.doActivate();
      */
      currentStock = initialStock;

    }
  }


  @Override
  public void action() {
    // TODO Auto-generated method stub
    //MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    ACLMessage msg = myAgent.receive();

    if (msg==null){
      checkStock();
    }

    if (msg != null) {
      AID sender = msg.getSender();
      if (msg.getPerformative() == ACLMessage.REQUEST) {
        //System.out.println("Gettin Request");
        //AID sender = msg.getSender();
        //ACLMessage response = new ACLMessage(ACLMessage.AGREE);
        //response.addReceiver(sender);

        int require = Integer.parseInt((String)msg.getContent());
        ACLMessage response;

        if (require <= this.currentStock && require > 0){
          //System.out.println("Request Accepted");
           response = new ACLMessage(ACLMessage.AGREE);
          response.setContent("available");

        }
        else{
          response = new ACLMessage(ACLMessage.REFUSE);
          //System.out.println("Request Rejected");
          response.setContent("not-available");
        }


        if (!isAssigned){
          myAgent.send(response);
          System.out.println(myAgent.getLocalName() + "Stock   assigned to Agent:"+ sender.getLocalName());
          isAssigned = true;
        }
          //System.out.println("Sendingresponse" + ACLMessage.getPerformative(response.getPerformative()));

      }

      if (msg.getPerformative() == ACLMessage.CONFIRM) {
        System.out.println(myAgent.getLocalName() + "Stock   unassigned to Agent:"+ sender.getLocalName());
        int require = Integer.parseInt((String)msg.getContent());
        //System.out.println("Inform from " + msg.getSender());
        isAssigned = false;
        this.currentStock = this.currentStock - require;
      }
    }

  }

}
