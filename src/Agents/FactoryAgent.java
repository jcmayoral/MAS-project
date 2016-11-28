package Agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import Behaviour.FactoryAgentOneshotBehaviour;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

@SuppressWarnings("serial")
public class FactoryAgent extends Agent {

	private Vector<AID> TransportAgents = new Vector<AID>();

	protected void setup() {

		System.out.println("Factory Agent:" + getAID().getName() + "is Initialized");

		// register for service
		// DFAgentDescription dfd = new DFAgentDescription();
		// dfd.setName(getAID());
		// ServiceDescription sd = new ServiceDescription();
		// sd.setType("Transport-Items");
		// sd.setName("Transport-Items");
		// dfd.addServices(sd);
		//
		// try
		// {
		// DFService.register(this, dfd);
		// }
		// catch(FIPAException fe){
		// fe.printStackTrace();
		// }

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
					//System.out.println(result.length);
					//TransportAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						TransportAgents.add(result[i].getName());
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				PrintAgentList();
			}
		});

		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick() {
				// Fill the CFP message
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < TransportAgents.size(); ++i) {
					msg.addReceiver(TransportAgents.get(i));
				}
				msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
				// We want to receive a reply in 10 secs
				// msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				msg.setContent("dummy-action");

				addBehaviour(new ContractNetInit(this.getAgent(), msg));
			}
		});


	}

	protected void takeDown() {
		System.out.println("Factory Agent:" + getAID().getName() + " is Terminated");
		// try{
		// DFService.deregister(this);
		// }catch(FIPAException fe){
		// fe.printStackTrace();
		// }
	}

	public void PrintAgentList() {
		for (int i = 0; i < TransportAgents.size(); i++) {
			System.out.println("TransportAgent["+(i+1)+"]:"+ TransportAgents.get(i));
		}
	}

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
		}

	}

}
