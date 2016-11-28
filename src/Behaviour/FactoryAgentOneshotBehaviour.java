package Behaviour;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class FactoryAgentOneshotBehaviour extends OneShotBehaviour{

	private AID[] TransportAgents;
	
	
	public AID[] getTransportAgentsList(){
		return TransportAgents;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Transport-Items");
		template.addServices(sd);
		
		//Fetch Agent List
		try {
			DFAgentDescription[] result = DFService.search(myAgent, template);
			TransportAgents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				TransportAgents[i] = result[i].getName();
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		PrintAgentList();
		
	}
	
	public void PrintAgentList(){
		for(int i=0; i < TransportAgents.length;i++){
			System.out.println("TransportAgent[i+1]: "+TransportAgents[i]);
		}
	}

}
