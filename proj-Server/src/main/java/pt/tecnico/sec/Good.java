package pt.tecnico.sec;

public class Good {
	
	private String id;
	private State state;
	
	private enum State {
		ONSALE,NOTONSALE
	}
	
	public Good() {
		state= State.NOTONSALE;
	}
	
	public State getState(){
		return this.state;
	}
	
	public void switchState() {
		if(state.equals(state.ONSALE)) {
			state= State.NOTONSALE;
		}
		else
			state= State.ONSALE;			
	}
	

}
