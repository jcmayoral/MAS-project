package Agents;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import Behaviours.StockServer;

//@SuppressWarnings("serial")
public class StockAgent extends Agent{

	protected void setup(){

		System.out.println("Stock Agent:" + getAID().getLocalName() + " is Initialized");

		// register for service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Stockpile-Info");
		sd.setName("Stockpile-Info");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		StockServer stockServer = new StockServer();
		stockServer.setStock(10);
		addBehaviour(stockServer);
	}

	protected void takeDown() {
		System.out.println("Stock Agent:" + getAID().getLocalName() + " is Terminated");

		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

	}
}
