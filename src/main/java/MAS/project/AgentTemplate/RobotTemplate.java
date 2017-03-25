package AgentTemplate;
import jade.core.Agent;
import jade.core.AID;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import Behaviours.ContractNetResp;

public class RobotTemplate extends Agent{

  protected List<AID> placeAgents = new ArrayList<AID>();
  protected ContractNetResp contractBehavior;

  public void PrintAgentList() {
    for (int i = 0; i < placeAgents.size(); i++) {
      System.out.println("StockAgent[" + (i + 1) + "]:" +
                          placeAgents.get(i).getLocalName());
    }
  }

}
