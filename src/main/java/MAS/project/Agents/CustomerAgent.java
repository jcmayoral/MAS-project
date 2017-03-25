package Agents;

import java.io.IOException;
import java.util.Random;

import Messages.CustomerOrder;
import Behaviors.PlaceOrderBehaviour;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.util.Vector;


public class CustomerAgent extends Agent{

	protected Vector<AID> FactoryAgents = new Vector<AID>();
	//private List<AID> FactoryAgent = null;
	protected int interval = 500;
	protected PlaceOrderBehaviour ordersBehaviour;
	protected Object[] args;

	protected void setup() {
		System.out.println(getClass()+getAID().getLocalName()+" is Initialized");

		//OrdersParser Uses args
		args = getArguments();

		addBehaviour(new WakerBehaviour(this,5000) {

			protected void onWake() {
				// perform operation X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Order-Items");
				template.addServices(sd);

				// Fetch Agent List

				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if(result.length != FactoryAgents.size()){
						FactoryAgents.clear();
						for (int i=0;i<result.length;i++){
							FactoryAgents.add(result[i].getName());
						}
						updateFactoryAgents();
					}

				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				reset();

				}


		});

		ordersBehaviour = new PlaceOrderBehaviour(this,interval);
		addBehaviour(ordersBehaviour);
	}

	protected void updateFactoryAgents(){
		System.out.println(getClass() + "UPDATING FACTORY AGENTS");
		ordersBehaviour.setFactoryAID(FactoryAgents);
	}

	protected void takeDown(){
		System.out.println("Customer Agent:"+getAID().getLocalName()+"is Terminated");
	}
}
