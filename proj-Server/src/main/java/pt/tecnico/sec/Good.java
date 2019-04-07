package pt.tecnico.sec;

import java.util.HashMap;
import java.security.Signature;


public class Good {
	
	private String id;
	private State state;
	private int counter;
	private HashMap<String,Signature> signatures = new HashMap<String,Signature>();
	
	private enum State {
		ONSALE,NOTONSALE
	}
	
	public Good() {
		state= State.NOTONSALE;
	}
	
	public Good(String line) {
		id = line;
	}

	public State getState(){
		return this.state;
	}
	
	public Signature getSignature(String userID) {
		return signatures.get(userID);
	}
	
	public void updateSignature(String userID,Signature sig) {
		signatures.replace(userID,sig);
	}
	
	public int getCounter() {
		return this.counter;
	}
	
	public void updateCounter() {
		counter++;
	}
	
	public void switchState() {
		if(state.equals(state.ONSALE)) {
			state= State.NOTONSALE;
		}
		else
			state= State.ONSALE;			
	}
	
		public String getID() {
			return this.id;
		}
	

}
