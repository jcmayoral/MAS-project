package Agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.TickerBehaviour;
import Behaviour.CustomerOrder;
import Behaviour.FactoryAgentOneshotBehaviour;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.util.leap.HashMap;
import jade.domain.FIPANames;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import java.util.Enumeration;

@SuppressWarnings("serial")
public class FactoryAgent extends Agent {

	private List<AID> transportAgents = new ArrayList<AID>();
	private PriorityQueue<CustomerOrder> orderQueue = new PriorityQueue<CustomerOrder>();
	private ThreadedBehaviourFactory tbf = new 	ThreadedBehaviourFactory();
	protected void setup() {

		System.out.println("Factory Agent:" + getAID().getName() + "is Initialized");

		// register for service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Order-Items");
		sd.setName("Place-Orders");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Fetch the list of available agents
		addBehaviour(new OneShotBehaviour(this) {

			@Override
			public void action() {
				// TODO Auto-generated method stub
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Transport-Items");
				template.addServices(sd);

				// Fetch Agent List
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					// System.out.println(result.length);
					// TransportAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						transportAgents.add(result[i].getName());
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				PrintAgentList();
			}
		});
//	      SequentialBehaviour seq = new SequentialBehaviour(this);
//	      addBehaviour( seq );
//		// prepare order queue
//	      seq.addSubBehaviour(new UpdateOrderQueue());
//	      seq.addSubBehaviour(new ProcessOrderQueue());
		addBehaviour(new UpdateOrderQueue());
		addBehaviour(new ProcessOrderQueue(this,5000));
		// complete the orders
//		seq.addSubBehaviour((new TickerBehaviour(this, 100) {
//			protected void onTick() {
//				if (orderQueue.size() != 0) {
//
//					//for (int i = 0; i < orderQueue.size(); i++) 
//					{
//						// Fill the CFP message
//						ACLMessage msg = new ACLMessage(ACLMessage.CFP);
//						for (int j = 0; j < transportAgents.size(); ++j) {
//							msg.addReceiver(transportAgents.get(j));
//						}
//						msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
//						// We want to receive a reply in 10 secs
//						// msg.setReplyByDate(new
//						// Date(System.currentTimeMillis() +
//						// 10000));
//						 msg.setContent("dummy order");
////						try {
////							msg.setContentObject(orderQueue.get(0));
////							
////						} catch (IOException e) {
////							// TODO Auto-generated catch block
////							e.printStackTrace();
////						}
//						addBehaviour(new ContractNetInit(this.getAgent(), msg));
//
//					}
//
//				} else {
//					System.out.println("Order queue is empty");
//				}
//
//			}
//		}));
		
		

	}

	protected void takeDown() {
		System.out.println("Factory Agent:" + getAID().getName() + " is Terminated");
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	public void PrintAgentList() {
		for (int i = 0; i < transportAgents.size(); i++) {
			System.out.println("TransportAgent[" + (i + 1) + "]:" + transportAgents.get(i));
		}
	}
	
	//Inner class to process orders
	private class ProcessOrderQueue extends TickerBehaviour{

		public ProcessOrderQueue(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onTick() {
			// TODO Auto-generated method stub
			if (orderQueue.size() != 0) {
				CustomerOrder order = orderQueue.element();
				//for (CustomerOrder order : orderQueue) {
					// Fill the CFP message
					ACLMessage msg = new ACLMessage(ACLMessage.CFP);
					for (int j = 0; j < transportAgents.size(); ++j) {
						msg.addReceiver(transportAgents.get(j));
					}
					msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
					// We want to receive a reply in 10 secs
					// msg.setReplyByDate(new
					// Date(System.currentTimeMillis() +
					// 10000));
					 //msg.setContent("dummy order");
					try {
						msg.setContentObject(order);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					Behaviour b = new ContractNetInit(this.getAgent(), msg);
//					addBehaviour(tbf.wrap(b));
					myAgent.addBehaviour(new ContractNetInit(this.getAgent(), msg));
					orderQueue.remove(order);
				//}
			} else {
				System.out.println("Order queue is empty");
			}
		}

	
	}

	// Inner class to handle order from the customers
	private class UpdateOrderQueue extends CyclicBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.REQUEST) {
					AID sender = msg.getSender();
					CustomerOrder order;
					try {
						order = (CustomerOrder) msg.getContentObject();
						System.out.println("Received order: " + order + "from :" + sender.getName());
						orderQueue.add(order);
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					block();

				}
			} 
		}

	}

	// Inner class to handle contract net communication
	private class ContractNetInit extends ContractNetInitiator {

		public ContractNetInit(Agent a, ACLMessage cfp) {
			super(a, cfp);
			// TODO Auto-generated constructor stub
		}

		protected void handlePropose(ACLMessage propose, Vector v) {
			System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
		}

		protected void handleRefuse(ACLMessage refuse) {
			System.out.println("Agent " + refuse.getSender().getName() + " refused");
		}

		protected void handleFailure(ACLMessage failure) {
			if (failure.getSender().equals(myAgent.getAMS())) {
				// FAILURE notification from the JADE runtime: the receiver
				// does not exist
				System.out.println("Responder does not exist");
			} else {
				System.out.println("Agent " + failure.getSender().getName() + " failed");
			}
			// Immediate failure --> we will not receive a response from
			// this agent
			// nResponders--;
		}

		protected void handleAllResponses(Vector responses, Vector acceptances) {
			// if (responses.size() < nResponders) {
			// // Some responder didn't reply within the specified timeout
			// System.out.println("Timeout expired: missing " + (nResponders -
			// responses.size()) + " responses");
			// }
			// Evaluate proposals.
			//System.out.println("handleAllResponses entered");
			int bestProposal = 10;
			AID bestProposer = null;
			ACLMessage accept = null;
			Enumeration e = responses.elements();
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();
				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
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
				System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			}
		}

		protected void handleInform(ACLMessage inform) {
			System.out
					.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
			
			//remove order from order queue
			try {
				CustomerOrder order = (CustomerOrder) inform.getContentObject(); 
				orderQueue.remove(order);
	
				//inform customer about the delivery
				AID customer = order.getSenderAID();
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(customer);
				myAgent.send(msg);
				
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
