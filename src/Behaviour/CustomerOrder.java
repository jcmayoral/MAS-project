package Behaviour;

import java.io.Serializable;

import jade.core.AID;

public class CustomerOrder implements Serializable{
	//bearing box 
	//bear
	//assembled bear box
	private AID customerID;
	private String order;
	
	public CustomerOrder(AID customerID,String order){
		this.customerID = customerID;
		this.order = order;

	}
	
	public String getOrder(){
		return order;
	}
	
	public AID getSenderAID(){
		return customerID;
	}
}
