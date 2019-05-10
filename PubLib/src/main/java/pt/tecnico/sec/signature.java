package pt.tecnico.sec;

import java.io.Serializable;

public class signature implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] sig;
	private String data;
	public signature(byte[] sig, String data) {
		this.sig = sig;
		this.data = data;
		
	}
	
	public byte[] getBytes() {
		return sig;
	}
	
	public String getData() {
		return data;
	}
}
