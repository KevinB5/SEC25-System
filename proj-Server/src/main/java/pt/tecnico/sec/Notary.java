package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Signature;
import java.util.HashMap;
import java.util.Scanner;

import pt.tecnico.sec.*;

public class Notary {
	
	private String idNotary = "notary" ;
	private static final String OK ="Ok";
	private static final String NOK ="Not OK";
	private HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	private HashMap<String, GoodState> states = new HashMap<String, GoodState>(); // <goodID,state>
	private HashMap<String, Integer> counters = new HashMap<String, Integer>(); // <goodID,counter>
	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	private Storage store;
	private PKI keyManager;
	
	public Notary() {
		store = new Storage();
		goods = store.getGoods();
		System.out.println(goods);
		for(String goodID: goods.keySet())
			states.put(goodID, GoodState.NOTONSALE);
	}
	
	String getID() {
		return this.idNotary;
	}

	
	private enum GoodState {
		ONSALE,NOTONSALE
	}
	
	 private boolean verifySignature(String data, byte[] signature, String uID) throws Exception {
		
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initVerify(PKI.getInstance().getKey(uID));
		sig.update(data.getBytes());
		
		return sig.verify(signature);
	}
	
	/**
	 * Ativar o servidor
	 
	public static void main(String[] args) {
		
	}*/
	
	/**
	 * Verificar o pedido de venda do user 
	 * @throws Exception 
	 */
	private String verifySelling(String userID, String goodID) throws Exception {
		System.out.println("Verifying "+goodID);
		if(!goods.containsKey(goodID))
			return "No such good";
		if(goods.get(goodID).equals(userID)) {
			states.replace(goodID, GoodState.ONSALE);
			counters.replace(goodID, counters.get(goodID)+1);
			System.out.println(states);
			
			return OK;
		}
		return NOK;
	}
	
	/**
	 * Verificar o estado de um good e retornar ao user
	 * @param goodID
	 * @param userID
	 */
	private String verifiyStateOfGood(String goodID) {
		if(!goods.containsKey(goodID))
			return "No such good";
		String state = "<";
		Integer counter;
		state += goods.get(goodID) + " , " + states.get(goodID).toString()+">";
		counter = counters.get(goodID);
		System.out.println(state+"|" +counter.toString());
		return state+"|" +counter.toString();
	}
	
	/**
	 * Retornar o estado do good
	 * 
	 * @param goodID
	 * @param userID
	 * @return Tuple com o id do good e o seu estado
	 *
	 */
//	private Object[] sendState( String goodID){
//		if(goods.containsKey(goodID)) {
//			Object[] pair = {goods.get(goodID), goods.get(goodID).getState()};
//			return pair;
//
//		}
//		return null;
//	}
//
	
	/**
	 * Ler os comandos do utilizador e realizar as operacoes respetivas
	 * @param command
	 * @throws Exception
	 */
	
	public Message execute(Message command) throws Exception {

		Message result = null;
    	String [] res = command.getText().split(" ");
    	if(res.length<2)
    		throw new Exception("Operation not valid: misgging arguments");
    	String data = "";
    	
    	for (int i=0; i<res.length -1; i++)
    		data+= res[i];
    	
    	String user = command.getID();
    	
		if(this.verifySignature(command.getText(), command.getSig(), command.getID())) {
			
			System.out.println("user's "+ user + " signature validated");
    	
	    //this.verifySignature(data, res[-1].getBytes(), user);
	    	
	    	//System.out.println(verifySignature(data, res[res.length-1].getBytes(), user));
	    	
	    	
	    	String op =  res[0];
	
	    	if(op .equals("sell")) {
	
	
	    		String rs=this.verifySelling(user, res[1]);//userID, goodID
	    		return new Message(this.idNotary, rs, null, null);
	    		}
	    	if(op.equals("state")) {
	    		String rs=  this.verifiyStateOfGood(res[1]);// goodID 2
	    		return new Message(this.idNotary, rs, null, null);
	
	    	}
	    	/*
	    	if(op.equals("buy"))
	    		this.buyGood(res[1]);*/
	    	
	    	if(op.equals("transfer")) {
	
	    		String rs=  this.transferGood(user,res[1],res[2]);//seller, buyer, goodID
	    		return new Message(this.idNotary, rs, null, null);
	
	    	}
	    	else
	    		return new Message(this.idNotary, "no valid operation", null,null);
	    	
		}
		else
			return null;
    	
	}
	/**
	 * Transferir o good ao user
	 * 
	 * @param goodID
	 * @return Transaction(?
	 *)
	 */
	private String transferGood( String seller,String buyer , String goodID) {
		if(goods.get(goodID).equals(seller)) {
			if(states.get(goodID).equals(GoodState.ONSALE)) {
				store.upDateFile(goodID, buyer);
				goods.replace(goodID, buyer); System.out.println("replacing " + goodID + " " + buyer);
				states.replace(goodID, GoodState.NOTONSALE);
				printGoods();
				counters.replace(goodID,counters.get(goodID)+1);
				return OK;	
			}
			else
				return "Good not on sale";
		}
		return NOK;
	}
	
	public void printGoods() {
		System.out.println(goods);
	}
	
	
}
