package Agents;

import java.io.IOException;
import Messages.CustomerOrder;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import AgentTemplate.RobotTemplate;
import Behaviours.ContractNetResp;

@SuppressWarnings("serial")
public class TransportAgent extends RobotTemplate {

	private UpdateStockInfo behavior;
	private boolean isStockAvailable = true;
	boolean isWaiting = false;

	protected void setup() {

		System.out.println("Transport Agent:" + getAID().getLocalName() + " is Initialized");

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

		addBehaviour(new WakerBehaviour(this,1000) {

			protected void onWake() {
				// perform operation X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Stockpile-Info");
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

		behavior = new UpdateStockInfo();
		//check stock status
		addBehaviour(behavior);
		// setup contract net responder
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		contractBehavior = new ContractNetResp(this,template);
		addBehaviour(contractBehavior);

	}

	protected void takeDown() {
		System.out.println("Transport Agent:" + getAID().getLocalName() + " is Terminated");
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	// Inner class to handle order from the customers
	private class UpdateStockInfo extends CyclicBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			//System.out.println("Places " + placeAgents.size());
			if (placeAgents.size() != 0) {
				ACLMessage msg = myAgent.receive();
				if (msg == null){
					ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
					CustomerOrder tmp = contractBehavior.getOrder();
					request.setContent(String.valueOf(tmp.getNumberPieces()));

					for (int i=0;i<placeAgents.size();i++)
						request.addReceiver(placeAgents.get(i));

					isWaiting = true;
					//System.out.println("Sending Request");
					send(request);
						//block();
				}
				//MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
				//ACLMessage msg = myAgent.receive(mt);
				//msg.getPerformative() == ACLMessage.PROPOSE

				if (msg != null && isWaiting) {
					System.out.println("Waiting" + ACLMessage.getPerformative(msg.getPerformative()) + msg.getSender());
					if (msg.getPerformative() == ACLMessage.AGREE){
						System.out.println("Agree");
						isStockAvailable = true;
						//System.out.println("Agree");
						ACLMessage inform = new ACLMessage(ACLMessage.CONFIRM);
						inform.addReceiver(msg.getSender());
						CustomerOrder tmp = contractBehavior.getOrder();
						inform.setContent(String.valueOf(tmp.getNumberPieces()));
						isWaiting = false;
						contractBehavior.updateParams(isStockAvailable);
						send(inform);
						System.out.println("AGREE");
						block();
					} else if (msg.getPerformative() == ACLMessage.REFUSE) {
						isStockAvailable = false;
						contractBehavior.updateParams(isStockAvailable);
						isWaiting = false;
					}
				}
			}
		}
	}

}
