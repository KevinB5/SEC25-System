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
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

//import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.events.NotationDeclaration;

import java.security.*;


public class Notary {

public enum GoodState {
	ONSALE,NOTONSALE
}
	
	private String idNotary  ;
	private static final String OK ="OK";
	private static final String NOK ="Not OK";
	private static final String ACK = "ACK";
	private HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	private HashMap<String, GoodState> states = new HashMap<String, GoodState>(); // <goodID,state>
	private HashMap<String, Integer> counters = new HashMap<String, Integer>(); // <goodID,counter>
	private HashMap<String,Integer> timestamps = new HashMap<String, Integer>();
	private HashMap<String,byte[]> signatures = new HashMap<String, byte[]>(); //<goodID,signature>
//	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	private Storage store;
//	private PKI keyManager;
	private String PASS;
	private SLibrary lib;

	private KeyPair keypair = null;
	private final String hashLimit = "0000";
	
	
	public Notary(int id,Storage store) {
		idNotary = "notary"+ id;
		this.store = store;
		this.lib=new SLibrary(this);
		goods = store.getGoods();
		System.out.println(goods);
		for(String goodID: goods.keySet()) {
			states.put(goodID, GoodState.NOTONSALE);
			counters.put(goodID, 0);
			timestamps.put(goodID, 0);
		}
//		
//		PKI.getInstance();
//		PKI.createKeys(idNotary);
		Random random = new Random();	
		
		int rnd = random.nextInt();
		PASS = idNotary + rnd;
		
		PKI.getInstance();
		PKI.createKeys(idNotary,PASS);

		try {
			keypair = new KeyPair(PKI.getPublicKey(idNotary), (PrivateKey) PKI.getPrivateKey(idNotary, PASS));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	String getID() {
		return this.idNotary;
	}
	
	void setServers(HashMap<String,Socket> serv) {
		System.out.println("YEET");

	}
	
    public void connect(String userID, int Uport) {
    	lib.connect(userID, Uport);
    }
	
	void senMessage(String uID){
		System.out.println("SENDING");
		Message msg = new Message(this.idNotary, "OI "+ uID, null);
		try {
			lib.sendMessage(uID, msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		int novo = counters.get(goodID)+1;
		if(goods.get(goodID).equals(userID)) {
			states.replace(goodID, GoodState.ONSALE);
			counters.replace(goodID,novo );
			System.out.println(counters.get(goodID));
			
			return ACK;
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
		int counter;
		state += goods.get(goodID) + " " + states.get(goodID).toString();
		counter = counters.get(goodID);
		return state+" " +counter+" "+ challenge;
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
    	
    	String user = command.getID();
    	//System.out.println("signature verification: "+this.verifySignature(command.getText(), command.getSig(), command.getID()));
		if(PKI.verifySignature(command.getText(), command.getSig(), command.getID())) {
			
			System.out.println("user's "+ user + " signature validated");
			
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
	    		if(res[2].equals(counters.get(res[1]).toString())) {
	    			String ts = res[3];
		    		String rs=this.verifySelling(user, res[1]);//userID, goodID
		    		
		    		if(rs.equals("ACK")) {
		    			timestamps.put(res[1],Integer.parseInt(res[3]));
		    			signatures.put(res[1],command.getSig());
		    		}
		    		//System.out.println("Returning "+rs);
		    		return new Message(this.idNotary, rs+" "+ts, PKI.sign(rs,idNotary,PASS),null, null,null);
	    			}else
	    		return new Message(this.idNotary, "wrong counter", PKI.sign("wrong counter",idNotary,PASS),null, null,null);
	    		}
	    	if(op.equals("state")) {
	    		System.out.println(idNotary +": received getState request");
	    		if(res.length!=4) {
	    			System.out.println(idNotary+ ": request is wrong");
	    			String rs = "WARNING: State request must issue a challenge and rid";
	    			return new Message(this.idNotary, rs,PKI.sign(rs,idNotary,PASS), null,null,null);
	    		}
	    		else {
	    			System.out.println(idNotary+ ": sending state");
	    			String rs=  this.verifiyStateOfGood(res[1],res[2]);
	    			String mess = rs+ " "+ timestamps.get(res[1])+ " "+ res[3];
	    			return new Message(this.idNotary, mess , PKI.sign(mess,idNotary,PASS),null, null,null);
	    		}
	    	}
	    	if(op.equals("transfer")) {
	    		
	    		System.out.println("transfering "+res[2]+"...");
	    		//"transfer <buyerID> <goodID> <goodcounter>"
	    		System.out.println("Counters:" + counters);
	    		System.out.println("Counter from seller: "+Integer.parseInt(res[3]));
	    		if(Integer.parseInt(res[3]) == (counters.get(res[2]))){
	    				//"buy <userID> <goodID> <goodCounter>
	    			String rs=  this.transferGood(user,res[1],res[2],command.getSig(),command.buyerSignature());//seller, buyer, goodID
		    		if(!rs.equals(NOK)) {
		    			//eIDLib eid = new eIDLib();
		    			System.out.println("okay, writing cert");
		    			int days = 7;
						X509Certificate cert = PKI.generateCertificate(idNotary, rs, keypair, days, "SHA256withRSA");
			    		//cert = eid.getCert();
		    			//cert= null;
			    		//eid.sign(cert,rs);
			    		return new Message(this.idNotary, rs, PKI.sign(rs,idNotary,PASS),null, null,cert);
		    		}else
		    		return new Message(this.idNotary, "not valid transfer", PKI.sign("not valid transfer",idNotary,PASS),null, null,null);
	    		}else
	    		return new Message(this.idNotary, "wrong counter", PKI.sign("wrong counter",idNotary,PASS),null, null,null);
	    	}else
	    		return new Message(this.idNotary, "not valid operation", PKI.sign("not valid operation",idNotary,PASS),null, null,null);
		}else
			return new Message(this.idNotary, "signature not valid", PKI.sign("signature not valid",idNotary,PASS),null, null,null);
	}
	/**
	 * Transferir o good ao user
	 * 
	 * @param goodID
	 * @return Transaction(?
	 *)
	 * @throws Exception 
	 */
	private String transferGood( String seller,String buyer , String goodID,byte[] sigSeller,byte[]sigBuyer) throws Exception {
		//for(String s: goods.keySet()) {System.out.println(s);}

		if(goods.get(goodID).equals(seller)) {
//			System.out.println("SELLER OK "+ seller);
			if(states.get(goodID).equals(GoodState.ONSALE)) {
				if(PKI.verifySignature("intentionbuy "+goodID + " "+counters.get(goodID), sigBuyer, buyer)){
//				System.out.println("we in");
					store.writeLog(goodID,seller,buyer,""+counters.get(goodID),sigSeller,sigBuyer);
					store.updateFile(goodID, buyer);
					goods.replace(goodID, buyer); 
//				System.out.println("replacing " + goodID + " " + buyer);
					states.replace(goodID, GoodState.NOTONSALE);
					System.out.println(goods);
					counters.replace(goodID,counters.get(goodID)+1);
					//enviar certificado
					return goodID+" "+seller+" "+ buyer+" "+counters.get(goodID)+ 	" "+sigSeller + " "+sigBuyer;
				}else
					return NOK;
			}
			else
				return NOK;
		}
		return NOK;
	}
	
/*	
	public boolean verifyHash(String content,String hashValue) {
		content = content+hashValue;
		MessageDigest digest;
		byte[] hash = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
			content = DatatypeConverter.printHexBinary(hash);
			content = content.substring(0,4);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if(content.equals(hashLimit))
			return true;
		return false;
	}*/
}
