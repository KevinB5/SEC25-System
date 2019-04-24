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
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Notary {

public enum GoodState {
	ONSALE,NOTONSALE
}
	
	private String idNotary = "notary" ;
	private static final String OK ="OK";
	private static final String NOK ="Not OK";
	private HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	private HashMap<String, GoodState> states = new HashMap<String, GoodState>(); // <goodID,state>
	private HashMap<String, Integer> counters = new HashMap<String, Integer>(); // <goodID,counter>
//	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	private final String pathLog= System.getProperty("user.dir")+"\\src\\main\\java\\pt\\tecnico\\state\\transfer.log";
	private final ArrayList<String> log = new ArrayList<String>();
	private Storage store;
//	private PKI keyManager;
	private String PASS;
	
	
	public Notary() {
		store = new Storage();
		goods = store.getGoods();
		System.out.println(goods);
		for(String goodID: goods.keySet()) {
			states.put(goodID, GoodState.NOTONSALE);
			counters.put(goodID, 0);
		}
//		
//		PKI.getInstance();
//		PKI.createKeys(idNotary);
		Random random = new Random();	
		
		int rnd = random.nextInt();
		PASS = idNotary + rnd;
		
		PKI.getInstance();
		PKI.createKeys(idNotary,PASS);
	}
	
	String getID() {
		return this.idNotary;
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
	
	    		String rs=this.verifySelling(user, res[1]);//userID, goodID
	    		//System.out.println("Returning "+rs);
	    		return new Message(this.idNotary, rs, null,null, null,null);
	    		}
	    	if(op.equals("state")) {
	    		if(res.length!=3) {
	    			String rs = "WARNING: State request must issue a challenge";
	    			return new Message(this.idNotary, rs,null, null,null,null);
	    		}
	    		else {
	    			String rs=  this.verifiyStateOfGood(res[1],res[2]);
	    			return new Message(this.idNotary, rs, null,null, null,null);
	    		}
	    	}
	    	if(op.equals("transfer")) {
	    		
	    		System.out.println("transfering "+res[2]);
	    		//"transfer <buyerID> <goodID> <goodcounter>"
	    		if(Integer.parseInt(res[3]) == (counters.get(res[2]))){
	    				//"buy <userID> <goodID> <goodCounter>
	    			String rs=  this.transferGood(user,res[1],res[2],command.getSig(),command.buyerSignature());//seller, buyer, goodID
		    		if(!rs.equals(NOK)) {
		    			//eIDLib eid = new eIDLib();
		    			System.out.println("okay");
			    		X509Certificate cert = null;
			    		//cert = eid.getCert();
		    			//cert= null;
			    		//eid.sign(cert,rs);
			    		return new Message(this.idNotary, rs, null,null, null,cert);
		    		}else
		    		return new Message(this.idNotary, "not valid transfer", null,null, null,null);
	    		}else
	    		return new Message(this.idNotary, "wrong counter", null,null, null,null);
	    	}else
	    		return new Message(this.idNotary, "not valid operation", null,null, null,null);
		}else
			return new Message(this.idNotary, "signature not valid", null,null, null,null);
	}
	/**
	 * Transferir o good ao user
	 * 
	 * @param goodID
	 * @return Transaction(?
	 *)
	 */
	private String transferGood( String seller,String buyer , String goodID,byte[] sigSeller,byte[]sigBuyer) {
		//for(String s: goods.keySet()) {System.out.println(s);}
		System.out.println("goods owner "+ goods.get(goodID));

		if(goods.get(goodID).equals(seller)) {
//			System.out.println("SELLER OK "+ seller);
			if(states.get(goodID).equals(GoodState.ONSALE)) {
//				System.out.println("we in");
				store.updateFile(goodID, buyer);
				goods.replace(goodID, buyer); 
//				System.out.println("replacing " + goodID + " " + buyer);
				states.replace(goodID, GoodState.NOTONSALE);
				System.out.println(goods);
				counters.replace(goodID,counters.get(goodID)+1);
				writeLog(goodID,seller,buyer,""+counters.get(goodID),sigSeller,sigBuyer);
				//enviar certificado
				return goodID+" "+seller+" "+ buyer+" "+counters.get(goodID)+ 	" "+sigSeller + " "+sigBuyer;
			}
			else
				return NOK;
		}
		return NOK;
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
//				System.out.println(line);
				if(!line.startsWith("#")) {
					log.add(line);		
				}
			}
//			System.out.println("Log " + log);
		}catch(Exception e) {
			System.out.println("Error in reading state file: " + e.getMessage());
		}finally {
			scnr.close();
		}
	}
	
	
}
