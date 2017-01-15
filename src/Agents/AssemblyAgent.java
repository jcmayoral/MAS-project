package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Behaviour.CustomerOrder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
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

public class AssemblyAgent extends Agent {

	private int step = 0;
	private int proposal;
	private boolean isStockAvailable = true;
	private List<AID> assemblyPlaceList = new ArrayList<AID>();

	protected void setup() {
		System.out.println("Assembly Agent:" + getAID().getName() + " is Initialized");

		// Register Agent Services with DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Assemble-Bearing-Box");
		sd.setName("Assemble-Bearing-Box");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Fetch the list of available Assembly places
		// addBehaviour(new OneShotBehaviour(this) {
		//
		// @Override
		// public void action() {
		// // TODO Auto-generated method stub
		// DFAgentDescription template = new DFAgentDescription();
		// ServiceDescription sd = new ServiceDescription();
		// sd.setType("Forcefitting-machine-server");
		// template.addServices(sd);
		//
		// // Fetch Agent List
		// try {
		// DFAgentDescription[] result = DFService.search(myAgent, template);
		// // System.out.println(result.length);
		// // TransportAgents = new AID[result.length];
		// for (int i = 0; i < result.length; ++i) {
		// assemblyPlaceList.add(result[i].getName());
		// }
		// } catch (FIPAException fe) {
		// fe.printStackTrace();
		// }
		// PrintAgentList();
		// }
		// });
		addBehaviour(new ProcessAssemblyRequest());
		addBehaviour(new WakerBehaviour(this, 5000) {

			protected void handleElapsedTimeout() {
				// perform operation X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Forcefitting-machine-server");
				template.addServices(sd);

				// Fetch Agent List
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					// System.out.println(result.length);
					// TransportAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						assemblyPlaceList.add(result[i].getName());
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				PrintAgentList();
			}

		});

		// setup contract net responder
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		addBehaviour(new ContractNetResponder(this, template) {

			// private int step = 0;
			@Override
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				CustomerOrder order;
				try {
					order = (CustomerOrder) cfp.getContentObject();
					System.out.println("Assembly Agent " + getLocalName() + ": CFP received from Agent:"
							+ cfp.getSender().getLocalName() + "Order: " + order.getOrder());
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (isStockAvailable && (step == 0)) {
					// We provide a proposal
					// state = 1;
					proposal = evaluateAction();
					System.out.println("Assembly Agent " + getLocalName() + ": Proposing " + proposal);
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
				System.out.println("Assembly Agent " + getLocalName() + ": order accepted");
				// myAgent.addBehaviour(new ProcessAssemblyRequest());
				step = 1;
				if (performAction()) {

					// System.out.println("Assembly Agent " + getLocalName() +
					// ": completed processing the order");
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
					System.out.println("Assembly Agent " + getLocalName() + ": failed to process the order");
					throw new FailureException("unexpected-error");
				}
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				System.out.println("Assembly Agent " + getLocalName() + ": Proposal rejected");
			}
		});
	}

	protected void takeDown() {
		System.out.println("Assembly Agent:" + getAID().getName() + " is Terminated");
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	public void PrintAgentList() {
		for (int i = 0; i < assemblyPlaceList.size(); i++) {
			System.out.println("AssemblyPlace[" + (i + 1) + "]:" + assemblyPlaceList.get(i).getLocalName());
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
		// try {
		// Thread.sleep(proposal*1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return (Math.random() > 0.2);
		return true;
	}

	private class ProcessAssemblyRequest extends CyclicBehaviour {

		// private int step = 0;

		public void action() {
			// TODO Auto-generated method stub

			if (assemblyPlaceList.size() != 0) {
				switch (step) {
				case 1: {
					ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
					request.addReceiver(assemblyPlaceList.get(0));
					send(request);
					step = 2;
				}
					break;
				case 2: {
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
					ACLMessage msg = myAgent.receive(mt);
					if (msg != null) {
						String status = msg.getContent();
						if (status.compareTo("available") == 0) {
							System.out.println("Assembly agent " + myAgent.getLocalName() + ":Processing the order");
							step = 3;
						} else {
							step = 1;
						}
					}

				}
					break;
				case 3: {
					System.out.println("Assembly agent " + myAgent.getLocalName() + ":Processed the order");
					ACLMessage response = new ACLMessage(ACLMessage.INFORM);
					response.addReceiver(assemblyPlaceList.get(0));
					response.setContent("Returing the Forcefitting machine");
					send(response);
					step = 0;

				}
					break;
				}

			}
		}

	}

}
