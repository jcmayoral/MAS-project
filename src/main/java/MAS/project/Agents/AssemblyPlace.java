package Agents;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import Behaviours.ForceFittingMachineServer;

public class AssemblyPlace extends Agent{

	protected void setup(){

		System.out.println("Assembly place:" + getAID().getLocalName() + " is Initialized");

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

		addBehaviour(new ForceFittingMachineServer());
	}

	protected void takeDown(){

		System.out.println("Assembly place:" + getAID().getLocalName() + " is Terminated");
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

	}

}
