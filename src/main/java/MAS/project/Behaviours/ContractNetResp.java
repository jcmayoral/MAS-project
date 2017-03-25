package Behaviours;
import jade.core.Agent;
import jade.proto.ContractNetResponder;
import Messages.CustomerOrder;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import java.io.IOException;
import java.util.Random;


public class ContractNetResp extends ContractNetResponder{

  protected boolean busy = false;
  protected boolean isObjectAvailables = false;
  private int proposal;
  private CustomerOrder order = new CustomerOrder();

  public ContractNetResp(Agent a, MessageTemplate mt){
    super (a,mt);
  }

  public boolean isBusy(){
    return busy;
  }

  public void updateParams(boolean b){
    isObjectAvailables = b;
    busy = b;
  }

  public CustomerOrder getOrder(){
    return order;
  }

  public int getProposal(){
    return proposal;
  }

  @Override
  protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {

    try {
      order = (CustomerOrder) cfp.getContentObject();
      System.out.println("Responder order  " + order.getNumberPieces());
    } catch (UnreadableException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("here");
    //if(busy){
      //if (isObjectAvailables) {
      if(true){
      // We provide a proposal
      //state = 1;
      proposal = evaluateAction();
      /*
      System.out.println("Transport Agent: " + myAgent.getLocalName() +
                          ": Proposing " + proposal);
      */
      ACLMessage propose = cfp.createReply();
      propose.setPerformative(ACLMessage.PROPOSE);
      propose.setContent(String.valueOf(proposal));
      return propose;
    }
      else {
      //busy = false;
      // We refuse to provide a proposal

      System.out.println("Transport Agent: " +
                          myAgent.getLocalName() + ": Refuse");

      throw new RefuseException("evaluation-failed");
    }
  }

  @Override
  protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
      throws FailureException {
    //System.out.println("Transport Agent: " + myAgent.getLocalName() + ": order accepted");

    if (performAction()) {
      //System.out.println("Transport Agent: " + myAgent.getLocalName() + ": Completed processing the order");

      ACLMessage inform = accept.createReply();
      inform.setPerformative(ACLMessage.INFORM);

      try {

        CustomerOrder t = (CustomerOrder)cfp.getContentObject();
        t.setStatus(2);
        inform.setContentObject(t);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (UnreadableException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      busy = false;
      return inform;

    } else {
      busy = false;
      System.out.println("Transport Agent: " + myAgent.getLocalName() + ": Action execution failed");
      throw new FailureException("unexpected-error");
    }
  }

  @Override
  protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
    order = new CustomerOrder();
    busy = false;
    //System.out.println("Transport Agent: " + myAgent.getLocalName() + ": Proposal rejected");
  }

  private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		Random r = new Random();
		int Low = 1;
		int High = 3;
		int proposal = r.nextInt(High - Low) + Low;
		return proposal;

	}

  private boolean performAction() {
		//Simulate action execution by generating a random number
    order = new CustomerOrder();
		myAgent.doSuspend();
		try {
			Thread.sleep(proposal*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		myAgent.doActivate();
		// return (Math.random() > 0.2);
		return true;
	}

}
