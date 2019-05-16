package pt.tecnico.sec;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.crypto.Cipher;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;



public class User {
	private static String idUser ;
	
	private HashMap<String,GoodState> goods = new HashMap<String,GoodState>();
	private HashMap<String,Integer> counters = new HashMap<String,Integer>();
	private HashMap<String,String> usrPorts = new HashMap<String,String>();
	private ArrayList<String> allUsers = new ArrayList<String>();
	private Library lib;
	private static final String OK ="OK";
	private static final String NOK ="Not OK";
	private String ip;
	private static String line = System.getProperty("file.separator");
	private static final String path2 = originPath()+ line + "ports.txt";
	
	private static final String SELL = "sell";
    private static final String STATE = "state";
    private static final String BUY = "buy";
    private static final String TRANSFER = "transfer";

	private static ServerSocket serverSocket=null;
    private static int PORT;
	private int keySize ;
	private static String PASS;
	public static KeyStore KEYSTORE;
	public PublicKey notarypublickey;
	private String challenge;

	private int wts=0;
	private int rid=0;
	private Set<String> servs;
	JSONGood json = new JSONGood();


	
	
	/**
	 * Informar ao notary que quer vender um good
	 * @throws Exception 
	 */
	

public enum GoodState {
	ONSALE,NOTONSALE
}

	
	public User(String id, String ip) throws Exception {
		
		idUser = id;
		this.ip=ip;
		
		this.getPort();
		
		if(allUsers.contains(idUser)) {
		
			Storage store = new Storage(1);
			HashMap<String, Integer> h = store.readServs();
			servs= h.keySet(); 
			lib = new Library(this, ip,h);
			
//			HashMap<String, String> res =store.getGoods(id);
			List<String> res = json.getGoodList(id);

//			for(String good : res.keySet()) {
//				counters.put(good, 0);
//				if(res.get(good).equals("n"))
//					goods.put(good, GoodState.NOTONSALE);
//				else 
//					goods.put(good, GoodState.ONSALE);
//			}
			System.out.println(res);
			for(String good : res) {
				counters.put(good, 0);
				if(json.getGoodState(good,"").equals("NOTONSALE"))
					goods.put(good, GoodState.NOTONSALE);
				else 
					goods.put(good, GoodState.ONSALE);
			}
			
			printgoods();
			
			PASS = idUser;

			PKI.getInstance();
			
			PKI.createKeys(id, PASS);
			
		}else {
			throw new Exception();
		}
	}
	
	public int gtPort() {
		return this.PORT;
	}
	
	public String getID() {
		return this.idUser;
	}
	
	public void printgoods() {
		System.out.println(goods);
	}
	
	private static String originPath() {
		String origin = System.getProperty("user.dir");
		int lastBar = 0;
		for(int i=0; i < origin.length() ; i++) {
			if(origin.charAt(i)==line.charAt(0))
				lastBar=i;
		}
		return origin.substring(0,lastBar);
	}
	
	public void connectToUsers() {
		for(String id : usrPorts.keySet()) {
			lib.connectUser(ip, id, Integer.parseInt(usrPorts.get(id)));
		}		
	}
	
	
	private void getPort() {
			File text = new File(path2);
			String lines ="";

			Scanner scnr;
			try {
				scnr = new Scanner(text);
				while(scnr.hasNextLine()) {
					String line = scnr.nextLine();
					lines += " "+line;	
					if(line.charAt(0)==('#')) {
						allUsers.add(line.replace("#",""));
					}
				}
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			String [] res = lines.split(" ");
			String uid ="";
			for (int i =0; i< res.length; i++) {

				if(i%2==0)
					uid=res[i];
				
				if(i%2!=0) {
					if(res[i].equals("#"+this.idUser)) {
						PORT = Integer.parseInt(res[i+1]);
						continue;
					}
					usrPorts.put(res[i].substring(1), res[i+1]);
				}
				
			}
		
	}
	
    public void readOperation(String operation) throws Exception {

    	String [] res = operation.split(" ");

    	String op =  res[0];
    	
    	if(op.equals("mygoods")) {
    		System.out.println(json.getMyGoodList(idUser.substring(-1, 0)));
    	}
    	
    	if(op.equals("sell")) {
    		if(res.length!=2) {
    			System.out.println("correct syntax: sell <goodID>");
    		}else {
    		/* Gets the State of the Good invisibly */
    		String good = res[1];
    		this.getStateOfGoodInvisible(good);
    		this.intentionToSell(res[1]);

    		}
    	}else
    	
    	if(op.equals("state"))
    		if(res.length!=2) {
    			System.out.println("correct syntax: state <goodID>");
    		}else {
    			this.getStateOfGood(res[1],false);
    		}
    	
    	if(op.equals("buy"))
    		/* "buy userID goodID" */
    		if(res.length!=3) {
    			System.out.println("correct syntax: buy <sellerID> <goodID>");
    		}else {
    			this.getStateOfGoodInvisible(res[2]);
    			  
    			this.buyGood(res[1], res[2]);
    		}
    	
    	if(op.equals("connect"))
    		this.connectToUsers();
    	
    	
    	
    }
    
	private void intentionToSell(String good) {
		String res="";
		try {
			ArrayList<Sell> tasks = new ArrayList<Sell>();
			ArrayList<Future<String>>fut = new ArrayList<Future<String>>();
			int i=0;
			for(String not : servs) {
				i++;
				tasks.add(new Sell( not, good, counters.get(good)));
				}
			
    		wts++;
    		ExecutorService executor = Executors.newWorkStealingPool();
    		
    		for(Future<String> answer : executor.invokeAll(tasks)) {
    			System.out.println(answer.get());
    			if(answer.get().equals(OK)) {
    				System.out.println(good+" is on sale");
    				goods.replace(good, GoodState.ONSALE);
    			}

    		}

    		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(res);
		
		
	}
	
	private String intTransfer(String buyer, String good, byte[] buyerSig, String text) {
		String res="";
		try {
			ArrayList<Transfer> tasks = new ArrayList<Transfer>();
			ArrayList<Future<String>>fut = new ArrayList<Future<String>>();
			int i=0;
			for(String not : servs) {
				i++;
				tasks.add(new Transfer( not,buyer, good, buyerSig, text));
				}
			
    		wts++;
    		ExecutorService executor = Executors.newWorkStealingPool();
    		
    		for(Future<String> answer : executor.invokeAll(tasks)) {
    			System.out.println(answer.get());
    			if(answer.get().equals(OK)) {
    				System.out.println("Replacing state of good");
    				//goods.replace(good, GoodState.ONSALE);
    				res= answer.get();
    			}
    		}
    		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		return res;
	}

	private void ownerGood(String good) throws Exception {
		
//		getStateOfGood(good, true);
		wts++;
		
		String msg = "owner "+good +" "+counters.get(good)+" "+wts;
		
		System.out.println("message: "+msg);
		
		signature[] sigs = new signature[3];//propria write buyer
		sigs[1]= new signature(PKI.sign(msg, idUser, PASS), msg);
		
		
		Recorded rec = new Recorded("", counters.get(good), wts);
		
		Message result =  new Message(idUser, msg,sigs,rec,null);
		byte[] sig =PKI.sign(result.getHash(), idUser, PASS);
		
		result.setSignature(
				new signature(
						sig, result.getHash()));

//		String ret= lib.write(result, wts);
	}
	
	
	
	
	
	
	/**
	 * Informar ao notary que quer comprar um dado good
	 * 
	 * @param good
	 * @param  
	 * @throws Exception 
	 * @throws InvalidKeyException 
	 */
	private void buyGood (String user, String good) throws InvalidKeyException, Exception {
//		this.getStateOfGoodInvisible(good);
		int counter;
		if(counters.containsKey(good))
			 counter = counters.get(good);
		else 
			counter=0;
		System.out.println("trying to buy "+good+ " from "+user+"with counter "+ counter);

		Message res= null;
//		for(String c : counters.values())
//			System.out.println(c);
		if(counter==-1) {
			System.out.println("Need to verify state first");
			return;
		}
		try {
			String msg = "intentionbuy " +good +" "+counter;
    		Recorded rec = new Recorded("", counter, 0);

			//manda 
			System.out.println("Asking to buy "+good+ " from "+user);
	    	signature[] sigs = new signature[3];//propria write buyer
//	    	assinatura de buy
	    	sigs[2]= new signature(PKI.sign(msg,idUser,PASS), msg);
	    	Message mss = new Message(idUser, msg,sigs , rec, null);
	    	
	    	mss.setSignature(
	    			new signature(
	    					PKI.sign(mss.getHash(), idUser, PASS), mss.getHash()
	    					
	    			));

			res= lib.sendMessage(user,mss);
//			buyGood(user, good,counter);
			//res= lib.sendMessage(user,new Message(idUser, msg,sigs , rec, null));

			
			if(res.getText().equals(OK)) {
				System.out.println("yeeeyeee");
				goods.put(good, GoodState.NOTONSALE);
				printgoods();
				if(counters.get(good)>wts)
					wts=counters.get(good);
				ownerGood(good);
			}else {
				System.out.println("Not OK");
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		} catch(Exception e) {
			System.out.println(/*e.getLocalizedMessage()*/"Must connect to user first");
		}
		
	}
	
	/**
	 * Transferir um dado good
	 * 
	 * @param good
	 * @throws Exception 
	 * @throws InvalidKeyException 
	 */
	/*
	private String transferGood(String buyer, String good, byte[] buyerSig, String text) throws InvalidKeyException, Exception {
		this.getStateOfGoodInvisible(good);
		wts++;
		String res= "";
		String msg=TRANSFER +" "+idUser+" "+ buyer+" "+ good +" "+counters.get(good)+" "+wts; 
		try {
			int counter=0;
			
			if(counters.containsKey(good))
				counter = counters.get(good);
			
			signature[] sigs = new signature[3];//propria write buyer
	    	sigs[1]=null;
	    	sigs[2]=new signature(buyerSig, text);	
			
    		Recorded rec = new Recorded("",counter, wts);
    		
    		Message result =  new Message(idUser, msg,sigs,rec,null);
    		result.setSignature(
    				new signature(
    						PKI.sign(result.getHash(), idUser, PASS), result.getHash())
    				);
			
//	    	System.out.println("transfering with counter: "+counter);


//			res= lib.write(new Message(idUser, msg,sigs , rec, null), wts);

			System.out.println("answer from notary: "+res);
			if(res.equals(OK)) {
				goods.remove(good);
				printgoods();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
		
	}
*/

	private class Sell implements Callable<String>{
		//private PKI pki;
		private String goodID;
		private int integer;
		String id;
		Sell(String id, String goodID, Integer integer){
			this.goodID=goodID;
			this.integer=integer;
			this.id=id;
		}
		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			String ret ="";
			String msg =SELL +  " " +goodID + " " +counters.get(goodID) +" "+wts;
			System.out.println("sending message:"+ msg);
			try {
				signature[] sigs = new signature[3];//propria write buyer
		    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);////important
		    	sigs[1] = new signature(PKI.sign(msg,idUser,PASS), msg);

	    		Recorded rec = new Recorded("", integer, wts);
	    		Message message =  new Message(idUser, msg,sigs , rec, null);
	    		
	    		message.setSignature(
	    				new signature(
		    				PKI.sign(
		    						message.getHash(), idUser,PASS) , message.getHash()
		    				
	    				));
				ret= lib.write(id,message, wts);
				//System.out.println("received: "+ret);

			}catch(Exception e) {
				e.printStackTrace();;
			}
			return ret;
			//return null;
		}
	}
	
	private class Transfer implements Callable<String>{
		String server;
		String buyer;
		String good;
		byte[] buyerSig;
		String text;
		
		
		Transfer(String server, String buyer, String good, byte[] buyerSig, String text){
			this.server = server;
			this.buyer = buyer;
			this.good = good;
			this.buyerSig =buyerSig;
			this.text = text;
			
		}

		@Override
		public String call() throws Exception {
			String res= "";
			try {
				String msg=TRANSFER +" "+ buyer+" "+ good+" "+counters.get(good) +" "+wts; 
				signature[] sigs = new signature[3];//propria write buyer
		    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);
		    	sigs[1]=null;
		    	sigs[2]=new signature(buyerSig, text);
		    	System.out.println("transfering with counter: "+counters.get(good));

	    		Recorded rec = new Recorded("", counters.get(good), wts);
	    		
	    		Message message =  new Message(idUser, msg,sigs , rec, null);
	    		
	    		message.setSignature(
	    				new signature(
		    				PKI.sign(
		    						message.getHash(), idUser,PASS) , message.getHash()
		    				
	    				));
				res= lib.write(server,message, wts);

				//res= lib.write(server,new Message(idUser, msg,sigs , rec, null), wts);
				//res=  lib.write( new Message(idUser, msg, PKI.sign(msg,idUser,PASS),buyerSig, null, null),wts);

				System.out.println("answer from notary: "+res);
				if(res.equals(OK)) {
//					System.out.println(res);
					//TODO: Mandar resposta ao Buyer
					goods.remove(good);
					printgoods();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return res;
		}
		
	}
	
//	private class Transfer implements Callable<String>{
//			String server;
//			String buyer;
//			String good;
//			byte[] buyerSig;
//			String text;
//			
//			
//			Transfer(String server, String buyer, String good, byte[] buyerSig, String text){
//				this.server = server;
//				this.buyer = buyer;
//				this.good = good;
//				this.buyerSig =buyerSig;
//				this.text = text;
//				
//			}
//	
//			@Override
//			public String call() throws Exception {
//				String res= "";
//				try {
//					String msg=TRANSFER +" "+ buyer+" "+ good +" "+wts; 
//					signature[] sigs = new signature[3];//propria write buyer
//			    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);
//			    	sigs[1]=null;
//			    	sigs[2]=new signature(buyerSig, text);
//			    	System.out.println("transfering with counter: "+counters.get(good));
//	
//		    		Recorded rec = new Recorded("", counters.get(good), wts);
//		    		
//		    		Message message =  new Message(idUser, msg,sigs , rec, null);
//		    		
//		    		message.setSignature(
//		    				new signature(
//			    				PKI.sign(
//			    						message.getHash(), idUser,PASS) , message.getHash()
//			    				
//		    				));
//					res= lib.write(server,message, wts);
//	
//					//res= lib.write(server,new Message(idUser, msg,sigs , rec, null), wts);
//					//res=  lib.write( new Message(idUser, msg, PKI.sign(msg,idUser,PASS),buyerSig, null, null),wts);
//	
//					System.out.println("answer from notary: "+res);
//					if(res.equals(OK)) {
//	//					System.out.println(res);
//						//TODO: Mandar resposta ao Buyer
//						goods.remove(good);
//						printgoods();
//					}
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				return res;
//			}
//			
//		}

	private class WriteBack implements Callable<String>{
		String server;
		Message intent;
		int timestamp;
		boolean invisible;
		String goodID;
		
		
		WriteBack(String server,String goodID, Message intent, int timestamp,boolean invisible){
			this.server=server;
			this.intent=intent;
			this.timestamp=timestamp;
			this.invisible = invisible;
			this.goodID= goodID;
		}

		@Override
		public String call() throws Exception {
			String WBResult = lib.write(server,intent, timestamp);
			System.out.println("WBResult: "+WBResult);
			return WBResult;
		}
		
	}
	
	/*
	public String intentionToSell(String userID, String goodID, Integer integer) throws InvalidKeyException, Exception {
//		this.getStateOfGoodInvisible(goodID);
>>>>>>> branch 'newmaster' of https://github.com/KevinB5/SEC25-System.git
		wts++;
		String ret ="";
		String msg =SELL +  " " +goodID + " " + counter + " " + wts;
		System.out.println("sending message:"+ msg);
		try {
			signature[] sigs = new signature[3];//propria write buyer
<<<<<<< HEAD
			sigs[1]= new signature(PKI.sign(msg, userID, PASS), msg);
			
    		Recorded rec = new Recorded("", counter, wts);
    		
    		Message result =  new Message(userID, msg,sigs,rec,null);
    		byte[] sig =PKI.sign(result.getHash(), userID, PASS);
    		
    		result.setSignature(
    				new signature(
    						sig, result.getHash()));
    		
			ret= lib.write(result, wts);
=======
	    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);
	    	sigs[1]=null;
	    	sigs[2]=null;

    		Recorded rec = new Recorded("", integer, 0);
			//ret= lib.write(new Message(idUser, msg,sigs , rec, null), wts);
>>>>>>> branch 'newmaster' of https://github.com/KevinB5/SEC25-System.git

		}catch(Exception e) {
			e.printStackTrace();;
		}
		return ret;
	}
*/
	
	public void getStateOfGood(String goodID, boolean invisible) throws InvalidKeyException, Exception {
		System.out.println("Entering getStateOfGood");
		rid++;
		challenge= generateRandomString(20);
		String msg= STATE + " " + goodID + " "+challenge + " "+ rid;

		try {
			int counter=0;
			
			if(counters.containsKey(goodID))
				counter = counters.get(goodID);
			
			signature[] sigs = new signature[3];//propria write buyer

	    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);
	    	
	    	System.out.println(sigs[0]==null);


	//    	Recorded rec = new Recorded("", counter, counter);
//    		
//    		Message mess =  new Message(idUser, msg,sigs,rec,null);
//    		
//    		byte[] sig = PKI.sign(mess.getHash(), idUser, PASS);
//    		
//    		mess.setSignature(
//    				new signature(
//    						sig, mess.getHash())
//    				);
//    		    		
//    		System.out.println("Sending read request with counter "+counter);
//			Message result= lib.read(mess,rid, challenge,goodID);
//			System.out.println("Read result: "+result.getText() + " counter "+result.getRec().getCounter());
//			
//    		
//			result.setSignature(new signature(PKI.sign("", idUser, PASS), ""));
			Recorded rec = new Recorded("", counter, -1);
    		
    		Message mess =  new Message(idUser, msg,sigs,rec,null);
    		
    		byte[] sig = PKI.sign(mess.getHash(), idUser, PASS);
    		
    		mess.setSignature(
    				new signature(
    						sig, mess.getHash())
    				);
    		    		
    		System.out.println("Sending read request");
			Message result= lib.read(mess,rid, challenge,goodID);
			
			System.out.println("Read result: "+result.getText());

			
			/*  WRITE-BACK HERE   */
			
			if(result.getText().equals("zerocounter")) {
				if(!invisible)
					System.out.println("STATE from notary:" +goodID+" "+result.getRec().state +" "+(result.getRec().counter+1));
			}else {
				
			
			int wbts= result.getRec().timestamp;
			
			result.setSignature(new signature(PKI.sign(result.getText(), idUser, PASS), result.getText()));
			
			/***MULTI- THREADED WB*****/
			
			ArrayList<WriteBack> tasks = new ArrayList<WriteBack>();
			ArrayList<Future<String>>fut = new ArrayList<Future<String>>();
			int i=0;
			for(String serv : servs) {
				i++;
				tasks.add(new WriteBack( serv,goodID, result, wbts, invisible));
				}
			
    		wts++;
    		ExecutorService executor = Executors.newWorkStealingPool();
    		String res="";
    		for(Future<String> answer : executor.invokeAll(tasks)) {
    			System.out.println(answer.get());
    			if(answer.get().equals(OK)) {
    				System.out.println("Replacing state of good");
    				//goods.replace(good, GoodState.ONSALE);
    				res= answer.get();
    			}
    		}
			
	
			
			if(res.equals("OK")) {
				if(!invisible) {
					int count;
					if(result.getText().split(" ")[0].equals("sell"))
						count=result.getRec().counter+1;
					else
						count = result.getRec().counter;
					System.out.println("STATE from notary:" +goodID+" "+result.getRec().state +" "+count);
				}
				if(!counters.containsKey(goodID))
					counters.put(goodID,result.getRec().counter);
				else
					counters.replace(goodID,result.getRec().counter);   
			}
				
			}

		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	
	private String generateRandomString(Integer n) {
		//n: size of string
		String randomstring = "";
		int i;
		for(i=0; i < n; i++){
			Random rnd = new Random();
			char c = (char) (rnd.nextInt(26) + 'a');
			randomstring+=c;
		}
		return randomstring;
		
	}
	
	
	public Message execute(Message command) throws Exception {
		//"correct syntax: buy <sellerID> <goodID>"

		String msg = command.getText();
    	String [] res = msg.split(" ");
    	if(res.length<2) {
    		System.out.println("Operation not valid: missing arguments");
    		return null;
    	}
    	String op =  res[0];
		signature[] sigs = new signature[3];//propria write buyer

    	
    	System.out.println("RECEIVED from "+ command.getID()+"message "+ op);
    	String error = "Not valid";


		System.out.println("Verifying signature of received message");


    	if(!PKI.verifySignature(command.getHash(), command.getSig().getBytes(), command.getID())) {
        	sigs[0]= new signature(PKI.sign(error,idUser,PASS), error);
        	sigs[1]=null;
        	sigs[2]=null;
    		Recorded rec = new Recorded("", -1, -1);
    		Message message =  new Message(idUser, error,sigs , rec, null);
    		
    		message.setSignature(
    				new signature(
	    				PKI.sign(
	    						message.getHash(), idUser,PASS) , message.getHash()
	    				
    				));

    		return message;}

    	
    	if(op.equals("intentionbuy")) {//buy goodID counter
    		String ret = "no such good";
    		System.out.println(command.getID()+" wants to buy "+ res[1]);
    		if(goods.containsKey(res[1])) {
    			getStateOfGoodInvisible(res[1]); 
    			String good =res[1];
    			
    			System.out.println("Asking notary...");
    			String rep = intTransfer(command.getID(), good, command.buyerSignature().getBytes(), command.getText());
//	    		System.out.println(rep);

    			if(rep.equals("OK")) {
    				System.out.println(rep);
    			}
	    			
	    		this.printgoods();
	        	sigs[0]= new signature(PKI.sign(rep,idUser,PASS), rep);
	        	sigs[1]=null;
	        	sigs[2]=null;
	    		int counter=counters.get(good);
	    		Recorded rec = new Recorded("", counter, 0);
	    		Message message =  new Message(idUser, rep,sigs , rec, null);
	    		
	    		message.setSignature(
	    				new signature(
		    				PKI.sign(
		    						message.getHash(), idUser,PASS) , message.getHash()
		    				
	    				));


	    		return message;//construtor que poe os restantes parametros a null automáticamente
	    		}
        	sigs[0]= new signature(PKI.sign(ret,idUser,PASS), ret);
    		Recorded rec = new Recorded("", -1, -1);
    		Message message =  new Message(idUser, ret,sigs , rec, null);
    		
    		message.setSignature(
    				new signature(
	    				PKI.sign(
	    						message.getHash(), idUser,PASS) , message.getHash()
	    				
    				));

    		return message;//construtor que poe os restantes parametros a null automáticamente
    	}

    	else {
        	String errOp = "no valid operation " + command.getText();
        	sigs[0]= new signature(PKI.sign(errOp,idUser,PASS), errOp);
    		Recorded rec = new Recorded("", -1, -1);
    		Message message =  new Message(idUser, errOp,sigs, rec,null );
    		
    		message.setSignature(
    				new signature(
	    				PKI.sign(
	    						message.getHash(), idUser,PASS) , message.getHash()
	    				
    				));

        	return message;
    	}

    		
	}

	public void getStateOfGoodInvisible(String goodID) throws InvalidKeyException, Exception {
		getStateOfGood(goodID,true);
		
	}
	
	
}



