package pt.tecnico.sec;

import java.awt.List;
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
//	private HashMap<String,byte[]> buyersigs = new HashMap<String,byte[]>();
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


	
	/**
	 * Conectar um cliente ao servidor (notary)
	
	public static void main(String[] args) {
		
	} */
	
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
			HashMap<String, String> res =store.getGoods(id);

			for(String good : res.keySet()) {
				counters.put(good, 0);
				if(res.get(good).equals("n"))
					goods.put(good, GoodState.NOTONSALE);
				else 
					goods.put(good, GoodState.ONSALE);



			};
			printgoods();
			
			Random random = new Random();	
			
			int rnd = random.nextInt();
			PASS = idUser + rnd;

			PKI.getInstance();
			PKI.createKeys(id, PASS);
			/*try {
				lib.sendKey(pub);
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid key");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
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
	
	
	/*
	
	private KeyPair createKeys(String userID) {
	    KeyPairGenerator keyGen;
	    KeyPair keyPair = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(this.keySize);
			keyPair = keyGen.generateKeyPair();
			
			PublicKey pubKey = keyPair.getPublic();
			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyPair;
	}*/
	
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
//						System.out.println("USER: "+line.replace("#",""));
					}
				}
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
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
    		printgoods();
    	}
    	
    	if(op.equals("sell")) {
    		if(res.length!=2) {
    			System.out.println("correct syntax: sell <goodID>");
    		}else {
    		/* Gets the State of the Good invisibly */
    		String good = res[1];
    		getStateOfGoodInvisible(good);
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
    	/*
    	if(op.equals("transfer")) {
    		if(res.length!=3)
        		System.out.println("correct syntax: transfer <buyerID> <goodID>");
    		this.transferGood(res[1], res[2]);

    	}else*/
    	
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
    		
    		executor.invokeAll(tasks)
    	    .stream()
    	    .map(new Function<Future<String>, Object>() {
				@Override
				public Object apply(Future<String> future) {
				    try {
				    	System.out.println(future.get());
				        return future.get();
				    }
				    catch (Exception e) {
				        throw new IllegalStateException(e);
				    }
				}
			});
    	    /*
    		for(Sell task : tasks) {
        		Future<String> future = executor.submit(task);
        		fut.add(future);

    		}
    		Thread.sleep(1000*10);
    		for(Future future: fut) {
    			if(future.isDone()) {
    				//System.out.println("waiting result");
    				System.out.println("result"+future.get());


    			}


    			}
    		*/
		
			//System.out.println("res is "+res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(res);
		if( res.equals(OK)) {
			goods.replace(good, GoodState.ONSALE);
			System.out.println(good +" is on sale");
		}
		
		
	}
	
	/**
	 * Perguntar ao notary o estado de um good
	 * 
	 * @param good
	 * @return estado do good
	 */
	/*
	private void getStateOfGood(String good) {
		try {
			//Random string of 20 lowercase characters
			challenge = generateRandomString(20);
			String s = getStateOfGood(good,challenge);
//			System.out.println("PUTTING COUNTER AT " + s);

			//update counter
			if(!counters.containsKey(good))
				counters.put(good,s);
			else
				counters.replace(good,s);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	/**
	 * Informar ao notary que quer comprar um dado good
	 * 
	 * @param good
	 * @param  
	 */
	private void buyGood (String user, String good) {
//		this.getStateOfGoodInvisible(good);
		int counter = counters.get(good);
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
	    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);

			res= lib.sendMessage(user,new Message(idUser, msg,sigs , rec, null));
//			buyGood(user, good,counter);

			
			if(res.getText().equals(OK)) {
				System.out.println("yeeeyeee");
				goods.put(good, GoodState.NOTONSALE);
				printgoods();
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
	 */
	private String transferGood(String buyer, String good, byte[] buyerSig, String text) {
//		this.getStateOfGoodInvisible(good);//updates counter
		wts++;
		String res= "";
		try {
			String msg=TRANSFER +" "+ buyer+" "+ good +" "+wts; 
			signature[] sigs = new signature[3];//propria write buyer
	    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);
	    	sigs[1]=null;
	    	sigs[2]=new signature(buyerSig, text);
	    	System.out.println("transfering with counter: "+counters.get(good));

    		Recorded rec = new Recorded("", counters.get(good), -1);

			//res= lib.write(new Message(idUser, msg,sigs , rec, null), wts);
			//res=  lib.write( new Message(idUser, msg, PKI.sign(msg,idUser,PASS),buyerSig, null, null),wts);

			System.out.println("answer from notary: "+res);
			if(res.equals(OK)) {
//				System.out.println(res);
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
//	
//	private void sellGood(String goodID) {
//		System.out.println("selling "+ goodID);
//	}
	
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
			String msg =SELL +  " " +goodID + " "+wts;
			System.out.println("sending message:"+ msg);
			try {
				signature[] sigs = new signature[3];//propria write buyer
		    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);
		    	sigs[1]=null;
		    	sigs[2]=null;

	    		Recorded rec = new Recorded("", integer, 0);
				ret= lib.write(id, new Message(idUser, msg,sigs , rec, null), wts);
				System.out.println("received: "+ret);

			}catch(Exception e) {
				e.printStackTrace();;
			}
			return ret;
			//return null;
		}
	}
	
	private class Transfer implements Callable<String>{

		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	private class State implements Callable<String>{

		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	/*
	public String intentionToSell(String userID, String goodID, Integer integer) throws InvalidKeyException, Exception {
//		this.getStateOfGoodInvisible(goodID);
		wts++;
		String ret ="";
		String msg =SELL +  " " +goodID + " "+wts;
		System.out.println("sending message:"+ msg);
		try {
			signature[] sigs = new signature[3];//propria write buyer
	    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);
	    	sigs[1]=null;
	    	sigs[2]=null;

    		Recorded rec = new Recorded("", integer, 0);
			//ret= lib.write(new Message(idUser, msg,sigs , rec, null), wts);

		}catch(Exception e) {
			e.printStackTrace();;
		}
		return ret;
	}
*/
	
	public String[] getStateOfGood(String goodID, boolean invisible) throws InvalidKeyException, Exception {
		rid++;
		challenge= generateRandomString(20);
		String msg= STATE + " " + goodID + " "+challenge + " "+ rid;
		String result= null;
		try {
			signature[] sigs = new signature[3];//propria write buyer
	    	sigs[0]= new signature(PKI.sign(msg,idUser,PASS), msg);
	    	sigs[1]=null;
	    	sigs[2]= null;
	    	int counter=0;
	    	
	    	if(counters.containsKey(goodID))
	    		counter = counters.get(goodID);

    		Recorded rec = new Recorded("", counter, -1);
    		

			result= lib.read(new Message(idUser, msg,sigs , rec, null),rid, challenge,goodID);

			/*  WRITE-BACK HERE   */
			
			
			System.out.println("ret equals: "+result);

				//result =lib.read( new Message(idUser, msg, PKI.sign(msg,idUser,PASS),null, null, null),rid,challenge,goodID);
				System.out.println("got from lib: "+result);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		if(!result.equals("NOT OK")) {
		String[] split = result.split(" ");
		if(!invisible) {
			// resul =  state + counter + ts
			System.out.println("STATE from notary:" +goodID+" "+result);	
		}
		//returns [state,counter]
		return split;
		}else
			return null;
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

    	/*
    	
    	if(command.getID()=="notary") {
    		//if notary is sending second key we update
    		if(command.getText()=="key2") {
    			//TODO: meter condição para verificar se foi assinado com a chave do CC
    			notarypublickey=(PublicKey) command.getObj();
    		}else
    		if(this.verifySignatureNotary(msg.getBytes(), command.getSig()));
    	}*/

		System.out.println("Verifying signature of received message");


    	if(!PKI.verifySignature(msg, command.getSig().getBytes(), command.getID())) {
        	sigs[0]= new signature(PKI.sign(error,idUser,PASS), error);
        	sigs[1]=null;
        	sigs[2]=null;
    		Recorded rec = new Recorded("", -1, -1);

    		return new Message(idUser, error,sigs, rec, null);}

    	
    	if(op.equals("intentionbuy")) {//buy goodID counter
    		String ret = "no such good";
    		System.out.println(command.getID()+" wants to buy "+ res[1]);
    		if(goods.containsKey(res[1])) {
    			getStateOfGoodInvisible(res[1]); 
    			String good =res[1];
    			
    			System.out.println("Asking notary...");
    			String rep = transferGood(command.getID(), good, command.getSig().getBytes(), command.getText());
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


	    		return new Message(idUser, rep, sigs, rec, null);//construtor que poe os restantes parametros a null automáticamente
	    		}
        	sigs[0]= new signature(PKI.sign(ret,idUser,PASS), ret);
    		Recorded rec = new Recorded("", -1, -1);

    		return new Message(idUser, ret, sigs, rec,null);//construtor que poe os restantes parametros a null automáticamente
    	}

    	else {
        	String errOp = "no valid operation " + command.getText();
        	sigs[0]= new signature(PKI.sign(errOp,idUser,PASS), errOp);
    		Recorded rec = new Recorded("", -1, -1);

        	return new Message(idUser, errOp,sigs, rec,null );
    	}

    		
	}
	/*
	public Message buyGood(String userID, String goodID, String counter) throws Exception {//buyerID, goodID
		String msg = "intentionbuy " +goodID +" "+ counter;
		//manda 
		Message res= lib.sendMessage(userID,new Message(idUser, msg, PKI.sign(msg,idUser,PASS),null, null, null));
		return res;
		//return sendMessage(userID,new Message(idUser, msg,user.sign(msg),null, null, null));
		//sendMessage(0,msg);
		
	}

	*/

	public void getStateOfGoodInvisible(String goodID) throws InvalidKeyException, Exception {		
		String good = goodID;
		String[] s = getStateOfGood(good,true);
		if(s==null) {
			System.out.println("communication failed");
		}else
			
			System.out.println("counter from notary :"+s[3]);
		if(!counters.containsKey(good))
            counters.put(good,Integer.parseInt(s[3]));
        else
            counters.replace(good,Integer.parseInt(s[3]));   	
	}
	
	
	/*
	public String transferGood(String buyer,String goodID, String counter, byte[] buyerSig) throws InvalidKeyException, Exception {
		String msg=TRANSFER +" "+ buyer+" "+ goodID +" "+ counter; 
		Message result=  lib.send( new Message(idUser, msg, PKI.sign(msg,idUser,PASS),buyerSig, null, null));
	
		return result.getText();
	}
*/
}



