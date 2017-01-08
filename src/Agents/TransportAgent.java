package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Behaviour.CustomerOrder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;

@SuppressWarnings("serial")
public class TransportAgent extends Agent {

	private int state = -1;
	private int proposal;
	private boolean isStockAvailable = true;
	
	private List<AID> stockAgents = new ArrayList<AID>();

	protected void setup() {

		System.out.println("Transport Agent:" + getAID().getName() + "is Initialized");

		// Register Agent Services with DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Transport-Items");
		sd.setName("Deliver Parts");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		// Fetch the list of available Stock agents
		addBehaviour(new OneShotBehaviour(this) {

			@Override
			public void action() {
				// TODO Auto-generated method stub
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Stockpile-Info");
				template.addServices(sd);

				// Fetch Agent List
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					// System.out.println(result.length);
					// TransportAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						stockAgents.add(result[i].getName());
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				PrintAgentList();
			}
		});

		//check stock status
		addBehaviour(new UpdateStockInfo(this,10000));
		// setup contract net responder
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		addBehaviour(new ContractNetResponder(this, template) {
			@Override
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				CustomerOrder order;
				try {
					order = (CustomerOrder) cfp.getContentObject();
					System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName()
							+ ". Order: " + order.getOrder());
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
				if (isStockAvailable) {
					// We provide a proposal
					//state = 1;
					proposal = evaluateAction();
					System.out.println("Agent " + getLocalName() + ": Proposing " + proposal);
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(String.valueOf(proposal));
					return propose;
				} else {
					// We refuse to provide a proposal
					System.out.println("Agent " + getLocalName() + ": Refuse");
					throw new RefuseException("evaluation-failed");
				}
			}

			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
					throws FailureException {
				System.out.println("Agent " + getLocalName() + ": Proposal accepted");
				state = -1;
				if (performAction()) {

					System.out.println("Agent " + getLocalName() + ": Action successfully performed");
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					try {
						inform.setContentObject(cfp.getContentObject());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return inform;
				} else {
					System.out.println("Agent " + getLocalName() + ": Action execution failed");
					throw new FailureException("unexpected-error");
				}
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				state = -1;
				System.out.println("Agent " + getLocalName() + ": Proposal rejected");
			}
		});
	}

	protected void takeDown() {
		System.out.println("Transport Agent:" + getAID().getName() + " is Terminated");
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	// Inner class to handle order from the customers
	private class UpdateStockInfo extends TickerBehaviour {

		public UpdateStockInfo(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			int step = 0;
			switch (step) {
			case 0: {
				ACLMessage checkStock = new ACLMessage(ACLMessage.REQUEST);
				checkStock.addReceiver(stockAgents.get(0));
				send(checkStock);
				step = 1;
			}
				break;
			case 1: {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					String status = msg.getContent();
					if(status.compareTo("available") == 0){
						isStockAvailable = true;
					}
					else{
						isStockAvailable = false;
					}
				}
				step = 0;
			}
				break;
			}
			
		}


	}
	
	public void PrintAgentList() {
		for (int i = 0; i < stockAgents.size(); i++) {
			System.out.println("StockAgent[" + (i + 1) + "]:" + stockAgents.get(i).getLocalName());
		}
	}

	private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		Random r = new Random();
		int Low = 1;
		int High = 10;
		int proposal = r.nextInt(High - Low) + Low;
		return proposal;
		
	}

	private boolean performAction() {
		// Simulate action execution by generating a random number
//		try {
//			Thread.sleep(proposal*1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// return (Math.random() > 0.2);
		return true;
	}
	

}
