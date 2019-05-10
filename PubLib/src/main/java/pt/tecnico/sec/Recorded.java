package pt.tecnico.sec;

import java.io.Serializable;

public class Recorded implements Serializable{

    
  	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String state;
  	  int counter;
  	  int timestamp;
  	  public Recorded(String state,int counter, int timestamp) {
  		  
  		  this.state=state;
  		  this.counter=counter;
  		  this.timestamp=timestamp;
  		  
  		  }
  	  public String getState() {
  		  return state;
  	  }
  	  public int getCounter() {
  		  return counter;
  	  }
  	  public int getTS() {
  		  return this.timestamp;
  	  }
  	  
  	  public void setTS(int ts) {
  		  this.timestamp = ts;
  	  }
  	
  
  class RecordSig {
	  byte[] sig;
	  int timestamp;
	  RecordSig(byte[] s, int t) {this.sig=s;this.timestamp=t;}
	}
}
