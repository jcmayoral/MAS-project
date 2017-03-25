package Behaviours;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;

public class ForceFittingMachineServer extends CyclicBehaviour {

  private boolean isAssigned = false;

  @Override
  public void action() {
    // TODO Auto-generated method stub
    //MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    ACLMessage msg = myAgent.receive();
    AID assignTo = new AID();

    /*
    if (msg==null){
      checkStock();
    }
    */

    if (msg != null) {
      AID sender = msg.getSender();

      if (msg.getPerformative() == ACLMessage.REQUEST) {
        //AID sender = msg.getSender();
        //ACLMessage response = new ACLMessage(ACLMessage.AGREE);
        //response.addReceiver(sender);

        int require = Integer.parseInt((String)msg.getContent());
        ACLMessage response;


        if (!isAssigned){
          response = new ACLMessage(ACLMessage.AGREE);
          System.out.println(myAgent.getLocalName()+" :Forcefitting-machine assigned to Agent:"+ sender.getLocalName());
          assignTo = sender;
          response.setContent("available");
          isAssigned = true;

        }
        else{
          response = new ACLMessage(ACLMessage.REFUSE);
          //System.out.println("Request Rejected");
          response.setContent("not-available");
        }
        response.addReceiver(sender);
        //System.out.println("Sending response" + ACLMessage.getPerformative(response.getPerformative()));
        myAgent.send(response);
        //block();
      }

      if (msg.getPerformative() == ACLMessage.CONFIRM && sender == assignTo && isAssigned) {
        ACLMessage confirm = new ACLMessage(ACLMessage.INFORM);
        System.out.println(myAgent.getLocalName()+" :Forcefitting-machine unassigned to Agent:"+ sender.getLocalName());
        confirm.setContent("complete");
        confirm.addReceiver(sender);
        //System.out.println("Sending response" + ACLMessage.getPerformative(response.getPerformative()));
        isAssigned = false;
        myAgent.send(confirm);
        //block();
        //int require = Integer.parseInt((String)msg.getContent());
        //System.out.println("Inform from " + msg.getSender());
        //this.currentStock = this.currentStock - require;
      }
    }

  }
}
