package Behaviours;
import jade.core.Agent;
import jade.core.AID;
import jade.proto.ContractNetInitiator;
import jade.lang.acl.ACLMessage;
import java.util.Vector;
import java.util.Enumeration;
import jade.lang.acl.UnreadableException;
import Messages.CustomerOrder;

public class ContractNetInit extends ContractNetInitiator {

  private CustomerOrder backup;
  public ContractNetInit(Agent a, ACLMessage cfp) {
    super(a, cfp);
    // TODO Auto-generated constructor stu
    try {

      CustomerOrder t = (CustomerOrder) cfp.getContentObject();
      if (t.getNumberPieces()!=0){
        backup = (CustomerOrder) cfp.getContentObject();
      }
    }

    catch (UnreadableException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  protected void handlePropose(ACLMessage propose, Vector v) {
    //System.out.println("Agent: " + propose.getSender().getLocalName() + " proposed " + propose.getContent());
    //skipNextResponses();
  }

  protected void handleRefuse(ACLMessage refuse) {
    //addElement(backup);
    //reset();

  }

  protected void handleFailure(ACLMessage failure) {
    if (failure.getSender().equals(myAgent.getAMS())) {
      // FAILURE notification from the JADE runtime: the receiver
      // does not exist
      System.out.println("Responder does not exist");
    } else {
      System.out.println("Agent: " + failure.getSender().getLocalName() + " failed");
    }
    // Immediate failure --> we will not receive a response from
    // this agent
    // nResponders--;
  }

  protected void handleAllResponses(Vector responses, Vector acceptances) {

    int bestProposal = 10;
    AID bestProposer = null;
    ACLMessage accept = null;
    Enumeration e = responses.elements();

    while (e.hasMoreElements()) {
      ACLMessage msg = (ACLMessage) e.nextElement();
      if (msg.getPerformative() == ACLMessage.PROPOSE) {
        ACLMessage reply = msg.createReply();
        //System.out.println("Received Proposal of " + msg.getSender());
        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
        acceptances.addElement(reply);
        int proposal = Integer.parseInt(msg.getContent());

        if (proposal < bestProposal) {
          bestProposal = proposal;
          bestProposer = msg.getSender();
          accept = reply;
        }
      }
    }
    // Accept the proposal of the best proposer
    if (accept != null) {
      //System.out.println(myAgent.getLocalName()+": Accepting proposal " + bestProposal + " from Agent: " + bestProposer.getLocalName());
      accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
    }
    else{
      addElement(backup);
    }
  }

  protected void handleInform(ACLMessage inform) {

    //remove order from order queue
    try {
      CustomerOrder order = (CustomerOrder) inform.getContentObject();
      if(order != null){

        //remove(order);
        System.out.println(myAgent.getLocalName()+" : Agent: " +
                          inform.getSender().getLocalName() +
                            " successfully processed the order: " +
                                order.getType() + order.getCustomerAID().getLocalName()
                                + " number " + order.getNumberPieces()
                                + " Status " + order.getStatus());

      }


    } catch (UnreadableException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void removeElement(CustomerOrder o){

  }

  protected void addElement(CustomerOrder o){

  }
}
