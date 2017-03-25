package Agents;

import java.io.IOException;
import Messages.CustomerOrder;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import AgentTemplate.RobotTemplate;
import Behaviours.ContractNetResp;

public class AssemblyAgent extends RobotTemplate {

	private int step = 0;
	private boolean isStockAvailable = true;
	boolean isWaiting = false;

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

		addBehaviour(new ProcessAssemblyRequest());
		addBehaviour(new WakerBehaviour(this, 5000) {

			protected void onWake() {
				// perform operation X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Forcefitting-machine-server");
				template.addServices(sd);

				// Fetch Agent List
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if (result.length != placeAgents.size()){
						placeAgents.clear();
						for (int i = 0; i < result.length; ++i) {
							placeAgents.add(result[i].getName());
						}
						//PrintAgentList();
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				reset();
			}

		});

		// setup contract net responder
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		contractBehavior = new ContractNetResp(this,template);
		addBehaviour(contractBehavior);
	}

	protected void takeDown() {
		System.out.println("Assembly Agent:" + getAID().getName() + " is Terminated");
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	private class ProcessAssemblyRequest extends CyclicBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			ACLMessage msg = myAgent.receive();

			if (placeAgents.size() != 0) {
				if (!contractBehavior.isBusy() && (msg == null) && (!isWaiting)){
					ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
					CustomerOrder tmp = contractBehavior.getOrder();
					request.setContent(String.valueOf(tmp.getNumberPieces()));

					for (int i=0;i<placeAgents.size();i++)
						request.addReceiver(placeAgents.get(i));

					if (tmp.getNumberPieces() != 0){
						isWaiting = true;
						send(request);
						block();
					}
				}
				//System.out.println(getAID().getLocalName() + "waiting ? " + isWaiting);
				//MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
				//ACLMessage msg = myAgent.receive(mt);
				//msg.getPerformative() == ACLMessage.PROPOSE

				if (msg != null && isWaiting) {

					if (msg.getPerformative() == ACLMessage.AGREE){
						isStockAvailable = true;
						//System.out.println("Agree");
						contractBehavior.updateParams(isStockAvailable);
						ACLMessage inform = new ACLMessage(ACLMessage.CONFIRM);
						inform.addReceiver(msg.getSender());
						CustomerOrder tmp = contractBehavior.getOrder();
						inform.setContent(String.valueOf(tmp.getNumberPieces()));
						//isWaiting = false;
						send(inform);
						block();

						//contractBehavior.updateParams(isStockAvailable);
					}else if (msg.getPerformative() == ACLMessage.INFORM) {
						isStockAvailable = false;
						contractBehavior.updateParams(isStockAvailable);
						isWaiting = false;
					} else if (msg.getPerformative() == ACLMessage.REFUSE) {
						isStockAvailable = false;
						contractBehavior.updateParams(isStockAvailable);
						//isWaiting = false;
					}
				}
			}
		}
	}

/*
	private class ProcessAssemblyRequest extends CyclicBehaviour {

		// private int step = 0;

		public void action() {
			// TODO Auto-generated method stub

			if (placeAgents.size() != 0) {
				switch (step) {
				case 1: {
					ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

					for (int i=0;i<placeAgents.size();i++)
						request.addReceiver(placeAgents.get(i));

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
							//System.out.println("Assembly agent " + myAgent.getLocalName() + ":Processing the order");
							step = 3;
						} else {
							step = 1;
						}
					}

				}
					break;
				case 3: {
					//System.out.println("Assembly agent " + myAgent.getLocalName() + ":Processed the order");
					ACLMessage response = new ACLMessage(ACLMessage.INFORM);
					response.addReceiver(placeAgents.get(0));
					response.setContent("Returing the Forcefitting machine");
					send(response);
					step = 0;

				}
					break;
				}

			}
		}

	}
	*/

}
