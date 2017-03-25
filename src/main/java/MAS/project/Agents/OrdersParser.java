package Agents;

import java.io.IOException;
import java.util.Random;

import Messages.CustomerOrder;
import Behaviours.ParserBehaviour;

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
import java.io.File;


public class OrdersParser extends Agent{

	protected Vector<AID> FactoryAgents = new Vector<AID>();
	protected Vector<CustomerOrder> CustomerOrders = new Vector<CustomerOrder>();
	//private List<AID> FactoryAgent = null;
	protected int interval = 500;
	ParserBehaviour parserBehaviour;
	protected Object[] args;
	String filePath;

	@SuppressWarnings("serial")
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

    //New in Params
    checkParams();
		parserBehaviour = new ParserBehaviour(this, interval);
		parserBehaviour.updateParams(filePath);
		addBehaviour(parserBehaviour);
	}


	protected void updateFactoryAgents(){
		System.out.println(getClass() + " UPDATING FACTORY AGENTS");
		parserBehaviour.setFactoryAID(FactoryAgents);
	}

  public void checkParams(){
      //Up|load
			args = getArguments();
			System.out.println(new File("").getAbsolutePath());

      if (args[0].toString().equals("")){
        System.err.println("File Path Not Provided...");
        this.doDelete();
      }
      else{
				filePath = new File("").getAbsolutePath().concat(args[0].toString());
				System.out.println("File Path: " + filePath);
      }
  }

	protected void takeDown(){
		System.out.println("Customer Agent:"+getAID().getLocalName()+" is Terminated");
	}
}
