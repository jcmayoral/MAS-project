package Agents;

import java.io.IOException;

import Behaviour.CustomerOrder;
import jade.core.Agent;
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

				proposal = evaluateAction();
				if (state == -1) {
					// We provide a proposal
					state = 1;
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
//					try {
//						inform.setContentObject(cfp.getContentObject());
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (UnreadableException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
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

	private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		return (int) (Math.random() * 10);
	}

	private boolean performAction() {
		// Simulate action execution by generating a random number
		try {
			Thread.sleep(proposal);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return (Math.random() > 0.2);
		return true;
	}

}
