package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Signature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class Notary {
	
	private String idNotary = "notary" ;
	private static final String OK ="Ok";
	private static final String NOK ="Not OK";
	private HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	private HashMap<String, GoodState> states = new HashMap<String, GoodState>(); // <goodID,state>
	private HashMap<String, Integer> counters = new HashMap<String, Integer>(); // <goodID,counter>
	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	private final String pathLog= System.getProperty("user.dir")+"\\src\\main\\java\\pt\\tecnico\\state\\transfer.log";
	private final ArrayList<String> log = new ArrayList<String>();
	private Storage store;
	private PKI keyManager;
	
	public Notary() {
		store = new Storage();
		goods = store.getGoods();
		System.out.println(goods);
		for(String goodID: goods.keySet()) {
			states.put(goodID, GoodState.NOTONSALE);
			counters.put(goodID, 0);
		}
		System.out.println(states);
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
	private String verifiyStateOfGood(String goodID, String challenge) {
		/*Returns "<goodID , ONSALE/NOTONSALE , goodcounter , challenge>"  */
		if(!goods.containsKey(goodID))
			return "No such good "+challenge;
		String state = "";
		Integer counter;
		state += goods.get(goodID) + " " + states.get(goodID).toString();
		counter = counters.get(goodID);
		System.out.println(state+" " +counter.toString());
		return state+" " +counter.toString()+" "+ challenge;
		// returns "<state , counter , challenge>"
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
    	String [] res = command.getText().split(" "); //received message broken up by spaces
    	if(res.length<2)
    		throw new Exception("Operation not valid: missing arguments"); //message has to have at least 2 words
    	String data = "";
    	
    	for (int i=0; i<res.length -1; i++)
    		data+= res[i];
    	
    	String user = command.getID();
    	
		if(this.verifySignature(command.getText(), command.getSig(), command.getID())) {
			
			System.out.println("user's "+ user + " signature validated");
    	
	    //this.verifySignature(data, res[-1].getBytes(), user);
	    	
	    	//System.out.println(verifySignature(data, res[res.length-1].getBytes(), user));
	    	
			
			/* Types of messages:
			 * 
			 * "sell <goodID>" - requests that a given good be put ONSALE
			 * 
			 * "state <goodID> <challenge>" - asks what the state (ONSALE/NOTONSALE, goodcounter) of a certain good is
			 * 
			 *  "transfer <goodID> <goodCounter> <buyerID> <buyerSig>" - requests that a given good is transfered
			 * ^TRANSFER SHOULD INCLUDE A PART WHERE THE MESSAGE FROM THE BUYER SHOWING HIS INTENTION IS INCLUDED
			 * */
	    	
	    	String op =  res[0]; //the first word is the operation required
	
	    	if(op .equals("sell")) {
	
	    		String rs=this.verifySelling(user, res[1]);//userID, goodID
	    		return new Message(this.idNotary, rs, null,null, null);
	    		}
	    	if(op.equals("state")) {
	    		/*
	    		 * Returns "ONSALE/NOTONSALE <goodcounter>"
	    		 * 
	    		 */
	    		if(res.length==2) {
	    			String rs = "WARNING: State request must issue a challenge";
	    			return new Message(this.idNotary, rs, null, null,null);
	    		}else if(res.length==3) {
	    			String rs=  this.verifiyStateOfGood(res[1],res[2]); 
	    			return new Message(this.idNotary, rs, null,null, null);
	    		}
	    		
	    		
	
	    	}
	    	/*
	    	if(op.equals("buy"))
	    		this.buyGood(res[1]);*/
	    	
	    	if(op.equals("transfer")) {
	    		//"transfer <goodID> <goodCounter> <buyerID> <buyerSig>"
	    		//below: first verifies counter number of seller and then confirms that buy signature is associated to a message
	    		//"buy <goodID> <goodCounter>" from Buyer
	    		if(res[2].equals(counters.get(res[1]).toString()) && 
	    				this.verifySignature("buy "+res[1]+" "+counters.get(res[1]).toString(), res[4].getBytes(), res[3])) {
	    			
	    		}
	
	    		String rs=  this.transferGood(user,res[3],res[1],command.getSig(),res[4].getBytes());//seller, buyer, goodID
	    		return new Message(this.idNotary, rs, null,null, null);
	
	    	}
	    	else
	    		return new Message(this.idNotary, "no valid operation", null,null,null);
	    	
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
	private String transferGood( String seller,String buyer , String goodID,byte[] sigSeller,byte[]sigBuyer) {
		if(goods.get(goodID).equals(seller)) {
			if(states.get(goodID).equals(GoodState.ONSALE)) {
				store.upDateFile(goodID, buyer);
				goods.replace(goodID, buyer); System.out.println("replacing " + goodID + " " + buyer);
				states.replace(goodID, GoodState.NOTONSALE);
				printGoods();
				counters.replace(goodID,counters.get(goodID)+1);
				writeLog(goodID,seller,buyer,""+counters.get(goodID),sigSeller,sigBuyer);
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
	
	private void writeLog(String goodId, String seller, String buyer,String counter , byte[] sigSeller,byte[] sigBuyer) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		String data = goodId+";"+seller+";"+buyer+";"+counter+";"+sigSeller+";"+sigBuyer;
		try {
			File file = new File(this.pathLog);
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
			bw.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void readLog() {
		File systemFile = new File(pathLog);
		Scanner scnr = null;
		try {
			scnr = new Scanner(systemFile);
			while(scnr.hasNextLine()) {
				String line = scnr.nextLine();
				System.out.println(line);
				if(!line.startsWith("#")) {
					log.add(line);		
				}
			}
			System.out.println("Log " + log);
		}catch(Exception e) {
			System.out.println("Error in reading state file: " + e.getMessage());
		}finally {
			scnr.close();
		}
	}
	
}
