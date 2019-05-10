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
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;
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
	private final int id;
	

	private KeyPair keypair = null;
	private final String hashLimit = "0000";
	
	
	public Notary(int id) {
		this.id=id;
		idNotary = "notary"+ id;
        store = new Storage(id);
        store.setLog(String.valueOf(id));
        store.readLog();		
        this.lib=new SLibrary(this);
        this.updateState();
		System.out.println(goods);

		for(String goodID: goods.keySet()) {
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
	
	private void updateState(){
		goods = store.getGoods(id);

		HashMap<String, String> cs = store.getNStates(id);
		
		for(String good: cs.keySet()) {
			System.out.println("getting state for good: "+ good + " "+cs.get(good));
			if(cs.get(good).equals("n"))
				states.put(good, GoodState.NOTONSALE);
			else 
				states.put(good, GoodState.ONSALE);
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
	
    /*
	void senMessage(String uID){
		System.out.println("SENDING");
		Message msg = new Message(this.idNotary, "OI "+ uID, null);
		try {
			lib.sendMessage(uID, msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	
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
	 * @return GoodID + userID + counter + challenge
	 */
	private Recorded verifiyStateOfGood(String goodID, String challenge) {
		/*Returns "<goodID , ONSALE/NOTONSALE , goodcounter , challenge>"  */
		if(!goods.containsKey(goodID)) {
			return null;
			}
		
		String state = "";
		int counter;
		state += goods.get(goodID) + " " + states.get(goodID).toString();
		counter = counters.get(goodID);
		
		Recorded result = new Recorded(state, counter, 0);
		return result;
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
    	String [] message = command.getText().split(" "); //received message broken up by spaces
    	if(message.length<2)
    		throw new Exception("Operation not valid: missing arguments"); //message has to have at least 2 words
    	
    	String user = command.getID();
    	signature[] sigs = new signature[3];//propria write buyer
    	String error ="";
    	//System.out.println("signature verification: "+this.verifySignature(command.getText(), command.getSig(), command.getID()));
		if(PKI.verifySignature(command.getText(), command.getSig().getBytes(), command.getID())) {
			
			System.out.println("user's "+ user + " signature validated");
			System.out.println("received: "+command.getText());
			
			/* Types of messages:
			 * 
			 * "sell <goodID>" - requests that a given good be put ONSALE
			 * 
			 * "state <goodID> <challenge>" - asks what the state (ONSALE/NOTONSALE, goodcounter) of a certain good is
			 * 
			 *  "transfer <goodID> <goodCounter> <buyerID> <buyerSig>" - requests that a given good is transfered
			 * ^TRANSFER SHOULD INCLUDE A PART WHERE THE MESSAGE FROM THE BUYER SHOWING HIS INTENTION IS INCLUDED
			 * */
	    	
	    	String op =  message[0]; //the first word is the operation required
	    	//array de assinaturas

	    	
	    	if(op .equals("sell")) {
	    		if(message[2].equals(counters.get(message[1]).toString())) {
	    			String ts = message[3];
		    		String rs=this.verifySelling(user, message[1]);//userID, goodID
		    		
		    		if(rs.equals("ACK")) {
		    			timestamps.put(message[1],Integer.parseInt(message[3]));
		    			signatures.put(message[1],command.getSig().getBytes());
		    		}
		    		String mess = rs+" "+ts;
		    		System.out.println("Returning "+mess);
		    		sigs[0]=  new signature(PKI.sign(mess,idNotary,PASS), mess);
		    		Recorded rec = new Recorded("", 0, Integer.parseInt(ts));
		    		return new Message(this.idNotary, mess,sigs,rec,null);
	    			}else
	    		error ="wrong counter";
	    		sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);

	    		return new Message(this.idNotary,error ,sigs, null,null);
	    		}
	    	if(op.equals("state")) {
	    		System.out.println(idNotary +": received getState request");
	    		if(message.length!=4) {
	    			System.out.println(idNotary+ ": request is wrong");
	    			String rs = "WARNING: State request must issue a challenge and rid";
	    			sigs[0] = new signature(PKI.sign(rs,idNotary,PASS), rs);
	    			return new Message(this.idNotary, rs,sigs, null,null);
	    		}
	    		else {
	    			System.out.println(idNotary+ ": sending state ");
	    			//cria um recorded para enviar o estado
	    			Recorded rec=  this.verifiyStateOfGood(message[1],message[2]);//goodID, userID , counter , challenge
	    			rec.setTS(timestamps.get(message[1])); 
	    			String mess ="state " + rec.getState() + " "+ message[2] + " "+ message[3];
	    			System.out.println("state: " + mess);
	    			sigs[0] = new signature(PKI.sign(mess, idNotary,PASS), mess);//// adicionar mais assinaturas
	    			return new Message(this.idNotary, mess , sigs, rec,null);
	    		}
	    	}
	    	if(op.equals("transfer")) {
	    		/* TRANSFER +" "+ buyer+" "+ good +" "+ counter+" " + wts;  */

    			String ts =message[4];
	    		System.out.println("transfering "+message[2]+"...");
	    		System.out.println("Counter from seller: "+Integer.parseInt(message[3]));
	    		if(Integer.parseInt(message[3]) == (counters.get(message[2]))){
	    				//"buy <userID> <goodID> <goodCounter>
	    			System.out.println("we in");
	    			String rs=  this.transferGood(user,message[1],message[2],command.getSig().getBytes(),command.buyerSignature().getBytes());//seller, buyer, goodID
		    		if(!rs.equals(NOK)) {
		    			//eIDLib eid = new eIDLib();
		    			System.out.println("okay, writing cert");
		    			int days = 7;
						X509Certificate cert = null;
			    		//cert = eid.getCert();
		    			//cert= null;
			    		//eid.sign(cert,rs);
						String mess = ACK ;
						sigs[0] = new signature(PKI.sign(mess,idNotary,PASS), mess);
			    		Recorded rec = new Recorded("", 0, Integer.parseInt(ts));

			    		return new Message(this.idNotary, mess, sigs, rec,cert);
		    		}else
		    			error = "notvalidtransfer "+ts;
		    		sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);
		    		return new Message(this.idNotary, error,sigs, null,null);
	    		}else
	    			error = "wrongcounter "+ts;
	    		sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);

	    		return new Message(this.idNotary, error,sigs, null,null);
	    	}else
	    		error = "notValidOperation";
	    		sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);

	    		return new Message(this.idNotary, error,sigs, null,null);
		}else
			
			error = "signatureNotValid";
		    sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);

		return new Message(this.idNotary, error,sigs, null,null);
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
		this.updateState();
System.out.println(goods.get(goodID));
		if(goods.get(goodID).equals(seller)) {
			System.out.println("SELLER OK "+ seller);
			if(states.get(goodID).equals(GoodState.ONSALE)) {
				System.out.println("YEAH YEAH YEAH");
				if(PKI.verifySignature("intentionbuy "+goodID + " "+counters.get(goodID), sigBuyer, buyer)){
					System.out.println("we in");
					store.writeLog(goodID,seller,buyer,""+counters.get(goodID),sigSeller,sigBuyer);
					store.updateFile(goodID, buyer);
					goods.replace(goodID, buyer); 
//				System.out.println("replacing " + goodID + " " + buyer);
					states.replace(goodID, GoodState.NOTONSALE);
					counters.replace(goodID,counters.get(goodID)+1);
					System.out.println("good counter: "+counters.get(goodID));

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
	}
}
