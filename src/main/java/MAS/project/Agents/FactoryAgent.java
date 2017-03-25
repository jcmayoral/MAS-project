package Agents;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import Messages.CustomerOrder;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import Behaviours.ContractNetInit;
import jade.domain.FIPANames;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("serial")
public class FactoryAgent extends Agent {

	private List<AID> transportAgents = new ArrayList<AID>();
	private List<AID> assemblyAgents = new ArrayList<AID>();
	private List<CustomerOrder> orderList = new ArrayList<CustomerOrder>();

	protected void setup() {

		System.out.println("Factory Agent:" + getAID().getLocalName() + " is Initialized");

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

		addBehaviour(new WakerBehaviour(this,5000) {

			protected void onWake() {
				// perform operation X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Transport-Items");
				template.addServices(sd);

				// Fetch Agent List
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);

					if (result.length!=transportAgents.size()){
						transportAgents.clear();
						for (int i = 0; i < result.length; ++i) {
							transportAgents.add(result[i].getName());
						}
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				reset();
				//PrintAgentList();
			}

		});
		addBehaviour(new WakerBehaviour(this,5000) {

			protected void onWake() {
				// perform operation X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Assemble-Bearing-Box");
				template.addServices(sd);

				// Fetch Agent List
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);

					if (result.length != assemblyAgents.size()){
						assemblyAgents.clear();
						for (int i = 0; i < result.length; ++i) {
							assemblyAgents.add(result[i].getName());
						}
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				reset();
			}

		});

		addBehaviour(new UpdateOrderQueue());
		addBehaviour(new ProcessOrderQueue(this,1000));
		Location l = this.here();//(Location)this.getContainerController().getContainerName();
		this.doClone(l, getLocalName()+"-R");

	}

	protected void afterClone(){
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
	}

	protected void takeDown() {
		System.out.println("Factory Agent:" + getAID().getLocalName() + " is Terminated");
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
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
			if (orderList.size() != 0 && transportAgents.size() != 0 && assemblyAgents.size() != 0) {
				CustomerOrder order = orderList.get(0);
				String orderType = order.getType();
     			// Fill the CFP message
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);
				if (orderType.compareTo("Bearing") == 0) {
					for (int j = 0; j < transportAgents.size(); ++j) {
						msg.addReceiver(transportAgents.get(j));

						//System.out.println("msg replyby " + msg.getReplyByDate());
					}
				}
				else{
					for (int j = 0; j < assemblyAgents.size(); ++j) {
						msg.addReceiver(assemblyAgents.get(j));
					}
				}

				//msg.setReplyByDate(new Date(System.currentTimeMillis() + 15000));
				msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

				try {
					msg.setContentObject(order);

				} catch (IOException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				}

				myAgent.addBehaviour(new ContractNetInit(this.getAgent(), msg){
					protected void removeElement(CustomerOrder o){
						//orderList.remove(o);
					}

					protected void addElement(CustomerOrder o){
						orderList.add(o);
					}
				});
				orderList.remove(0);
			}
				System.out.println(myAgent.getLocalName()+" Queue " + orderList.size());
			/*
			System.out.println(myAgent.getLocalName()+" :Order queue is empty");
			else {
			}
			*/
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

						orderList.add(order);
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					block();

				}
			}
		}

	}
}
