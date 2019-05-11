package pt.tecnico.sec;

import java.io.Serializable;

public class signature implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] sig;
	private String data;
	private byte[] hash;
	public signature(byte[] sig, String data) {
		this.sig = sig;
		this.data = data;
		
	}
	public signature(byte[] sig, byte[] data) {
		this.sig = sig;
		this.hash=data;
		
	}
	
	public byte[] getBytes() {
		return sig;
	}
	
	public String getData() {
		return data;
	}
	public byte[] getHash() {
		return hash;
	}
}
