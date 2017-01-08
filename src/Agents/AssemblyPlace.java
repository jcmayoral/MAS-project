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

public class AssemblyPlace extends Agent{
	
	protected void setup(){
		
		System.out.println("Assembly place:" + getAID().getName() + "is Initialized");

		// register for service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Forcefitting-machine-server");
		sd.setName("Forcefitting-machine-server");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new ForcefittingMachineServer());
	}
	
	protected void takeDown(){
		
		System.out.println("Assembly place:" + getAID().getName() + " is Terminated");
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}
	
	// Inner class to handle order from the customers
	private class ForcefittingMachineServer extends CyclicBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.REQUEST) {
					AID sender = msg.getSender();
					System.out.println("Forcefitting-machine assigned to Agent:"+ sender.getLocalName());
					ACLMessage response = new ACLMessage(ACLMessage.AGREE);
					response.addReceiver(sender);
					response.setContent("available");
					myAgent.send(response);
					block();

				}
			} 
		}

	}

}
