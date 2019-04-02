package pt.tecnico.sec;

public class Good {
	
	private String id;
	private State state;
	private int counter;
	
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
