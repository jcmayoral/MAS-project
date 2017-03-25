package Messages;

import java.io.Serializable;

import jade.core.AID;

public class CustomerOrder implements Serializable{
	//bearing box
	//bear
	//assembled bear box
	private AID customerID;
	private String type;
	//0 - Raw
	//1 - Ready To Assemble
	//2 - Finish
	private int status = 0;
	private int numberPieces = 0;

	public int getNumberPieces(){
		return numberPieces;
	}

	public CustomerOrder(){
		//System.out.println("Empty Constructor from " + this.getClass().getName());
	}

	public CustomerOrder(CustomerOrder other){
		this.customerID = other.customerID;
		this.type = other.type;
		this.numberPieces = other.numberPieces;
		this.status = 0;
	}

	public CustomerOrder(String[] parser){
		System.out.println("order" + parser[0] + "type " + parser[1] + "number" + parser[2]);
		this.customerID = new AID(parser[0],AID.ISLOCALNAME);
		this.type = parser[1];
		this.numberPieces = Integer.parseInt(parser[2]);
		this.status = 0;
	}

	public CustomerOrder(AID customerID,String type, int numberPieces){
		this.customerID = customerID;
		this.type = type;
		this.numberPieces = numberPieces;
		this.status = 0;
	}

	public void setStatus(int newStatus){
		status = newStatus;
	}

	public int getStatus(){
		return status;
	}

	public String getType(){
		return type;
	}

	public AID getCustomerAID(){
		return customerID;
	}
}
