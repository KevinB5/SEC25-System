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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.monitor.Monitor;
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
	private static final String first = "FirstStage";
	private static final String ECH = "Echo";
	private static final String RDY = "Ready";
	private long waitID;

	private HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	private HashMap<String, GoodState> states = new HashMap<String, GoodState>(); // <goodID,state>
	private HashMap<String, Integer> counters = new HashMap<String, Integer>(); // <goodID,counter>
	private HashMap<String,Integer> timestamps = new HashMap<String, Integer>();
	private HashMap<String,signature> writesignatures = new HashMap<String, signature>(); //<goodID,signature>
	private HashMap<String,byte[]> signatures = new HashMap<String, byte[]>(); //<goodID,signature>
//	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	private boolean sentEcho = false;
	private boolean sentReady = false;
	private boolean delivered = false;
	
	private HashMap<String, String> echos = new HashMap<String, String>();
	private HashMap<String, String> readies = new HashMap<String, String>();
	
	private Collection<String> servers ;
	
	private Storage store;
//	private PKI keyManager;
	private String PASS;
	private SLibrary lib;
	private final int id;
	private int f;
	private int N;
	

	private KeyPair keypair = null;
	private final String hashLimit = "0000";
	private int responses=0;

	private	JSONGood json = JSONGood.getInstance();

	
	public Notary(int id, Storage store,int f) {
		this.id=id;
		idNotary = "notary"+ id;
        this.store = store;
        store.setLog(String.valueOf(id));
        store.readLog();
        this.f = f;
        N=3*f+1;
        this.lib=new SLibrary(this);

        this.updateState();
		System.out.println(goods);

		for(String goodID: goods.keySet()) {
			counters.put(goodID, 0);
			timestamps.put(goodID, 0);
		}

		
		PASS = idNotary;
		
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
	
	void connect() {
		HashMap<String, Integer> h = store.readServs();
		servers= h.keySet();
		for(String server:h.keySet()) {
			if(!server.equals(this.idNotary)){
				lib.connect(server, h.get(server));
				}
			}
		resetBRB();

	}
	
	private void resetBRB() {
		for(String server:servers) {
			if(!server.equals(this.idNotary)){
				this.echos.put(server,"");
				this.readies.put(server,"");
			}
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
		int novo = counters.get(goodID);
		System.out.println(goods.get(goodID));
		if(goods.get(goodID).equals(userID)) {
			if(!states.get(goodID).equals(GoodState.ONSALE)) {
				novo++;
				System.out.println("replacing counter"+counters.get(goodID)+" for "+novo);
				counters.replace(goodID,novo );				
				states.replace(goodID, GoodState.ONSALE);
			}			
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
		
		int counter;
		String ownerstate = goods.get(goodID) + " " + states.get(goodID).toString();
		counter = counters.get(goodID);
		
//		Recorded result = new Recorded(state, counter, 0);
//		System.out.println("state verified");
		Recorded result = new Recorded(ownerstate, counter, timestamps.get(goodID));
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
	
	public synchronized Message execute(Message command) throws Exception {
		boolean err =false;

		Message result = null;
    	String [] message = command.getText().split(" "); //received message broken up by spaces
    	
    	if(message.length<2)
    		throw new Exception("Operation not valid: missing arguments"); //message has to have at least 2 words
    	
    	String user = command.getID();
    	signature[] sigs = new signature[3];//propria write buyer
    	String error ="";
    	//System.out.println("signature verification: "+this.verifySignature(command.getText(), command.getSig(), command.getID()));

    	if(PKI.verifySignature(command.getHash(), command.getSig().getBytes(), user)
    			|| PKI.verifySignature(command.getWriteSignature().getData(), command.getWriteSignature().getBytes(),user)) {
			
			System.out.println("user's "+ user + " signature validated");
//			System.out.println("received: "+command.getText());
//			System.out.println("SIGNATURE: "+command.getSig().getBytes());
			if(command.getWriteSignature()!=null)
				System.out.println("WSignature: "+command.getWriteSignature().getBytes());
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
	    	String good = message[1];
	    	
	    	
	    	if(op .equals("sell")) {
	    		this.startBroadCast(command.getText(), command.getSellSig());
	    		while(!delivered) {
	    			 try { 
	    				 System.out.println("waiting");
	    				 waitID=Thread.currentThread().getId();
	    	                wait();
	    	            } catch (InterruptedException e)  {
	    	                Thread.currentThread().interrupt(); 
	    	                System.out.println("BRB did not reach consensus , returning"); 
	    	                return null;
	    	            }
	    		}
	    		delivered=false;
	    		sentReady=false;


    			int counter = command.getRec().getCounter();
    			int ts = command.getRec().getTS();

    			System.out.println("COUNTERS: "+counter+" "+counters.get(good) );
    			
    			boolean correctCounter = (counter==counters.get(good)) || (states.get(good).equals(GoodState.ONSALE)); 
    			
    			
	    		if(ts>= timestamps.get(good) && correctCounter) {
	    			
		    		String rs=this.verifySelling(user, message[1]);//userID, goodID
		    		if(rs.equals("ACK")) {
		    			timestamps.put(good,ts);
		    			goods.put(good, user);
		    			System.out.println("SIGNATURE2: "+signatures.get(good));
		    			if(command.getWriteSignature()!=null)
		    				writesignatures.put(good,command.getWriteSignature());
		    		}
		    		String mess = rs+" "+ts;
		    		System.out.println("Returning "+mess);
		    		//sigs[0]=  new signature(PKI.sign(mess,idNotary,PASS), mess);
		    		Recorded rec = new Recorded("", counter, (ts));
		    		result =  new Message(this.idNotary, mess,sigs,rec,null);
		    		result.setSignature(
		    				new signature(
		    						PKI.sign(result.getHash(), idNotary, PASS), result.getHash())
		    				);
		    		return result;
	    			}else
	    		error ="wrong counter";
	    		sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);
	    		Recorded rec = new Recorded("", 0,ts );

	    		return new Message(this.idNotary,error ,sigs, rec,null);
	    		}
	    	if(op.equals("owner")) {
	    		String[] info = command.getWriteSignature().getData().split(" ");
	    		int ts =Integer.parseInt(info[3]);
	    		if(ts>=timestamps.get(good) && user.equals(goods.get(good))) {
	    			if(command.getWriteSignature()!=null)
	    				writesignatures.put(good,command.getWriteSignature());
	    			
		    			String mess = "ACK "+ts;
			    		System.out.println("Returning "+mess);
			    		Recorded rec = new Recorded("", counters.get(good), ts);
			    		result =  new Message(this.idNotary, mess,sigs,rec,null);
			    		result.setSignature(
			    				new signature(
			    						PKI.sign(result.getHash(), idNotary, PASS), result.getHash())
			    				);
			    		return result;
	    			
	    		}
	    			
	    	}
	    	if(op.equals("state")) {
//	    		System.out.println(idNotary +": received getState request");
	    		System.out.println(idNotary +": received getState request");
	    		
//	    		this.startBroadCast(command.getText());
//	    		while(!this.delivered==true) {
//	    			long delay = 1000*8L;
//	    		    timer.schedule(task, delay);
//	    		    return null;
//	    		    
//	    			//lançar um timer
//	    			//caso passe o timer mensagem é descartada
//	    			//o que acontece caso o atraso se deva à rede ou assim?
//	    			//períodos de espera muito longos will fuck up the system
//	    		}
	    		if(message.length!=4) {
	    			System.out.println(idNotary+ ": request is wrong");
	    			String rs = "WARNING: State request must issue a challenge and rid";
	    			//sigs[0] = new signature(PKI.sign(rs,idNotary,PASS), rs);
		    		Recorded rec = new Recorded("", -1, -1);
		    		
		    		result =  new Message(this.idNotary, rs,sigs,rec,null);
		    		//notario assina totalidade da mensagem
		    		result.setSignature(
		    				new signature(
		    						PKI.sign(result.getHash(), idNotary, PASS), result.getHash())
		    				);
		    		return result;

	    			//return new Message(this.idNotary, rs,sigs, rec,null);
	    		}
	    		else {
	    			System.out.println(idNotary+ ": sending state ");
	    			//cria um recorded para enviar o estado
	    			Recorded rec=  this.verifiyStateOfGood(message[1],message[2]);//goodID, userID , counter , challenge
	    			rec.setTS(timestamps.get(message[1])); 
	    			String mess ="state "+ rec.getState() + " "+ message[2] +  " "+ message[3];
	    			System.out.println(mess+", counter:"+rec.getCounter());
	    			sigs[1] = writesignatures.get(message[1]);
	    			sigs[2] = new signature(signatures.get(message[1]),"");
	    			result =  new Message(this.idNotary, mess,sigs,rec,null);
		    		result.setSignature(
		    				new signature(
		    						PKI.sign(result.getHash(), idNotary, PASS), result.getHash())
		    				);
		    		return result;
	    		}
	    	}
	    	if(op.equals("transfer")) {
	    		/* TRANSFER +" "+ buyer+" "+ good +" "+ counter+" " + wts;  */
	    		
	    		this.startBroadCast(command.getText(), command.getSellSig());
	    		while(!delivered) {
	    			 try { 
	    				 waitID=Thread.currentThread().getId();
	    	                wait();
	    	            } catch (InterruptedException e)  {
	    	                Thread.currentThread().interrupt(); 
	    	                System.out.println("BRB did not reach consensus , returning"); 
	    	                return null;
	    	            }
	    		}
	    		delivered=false;

    			String ts =message[3];
	    		System.out.println("transfering "+message[2]+"...");
	    		System.out.println("Counter from seller: "+command.getRec().getCounter()+ "counter here: "+counters.get(message[2]));
	    		if(command.getRec().getCounter() == (counters.get(message[2]))){
	    				//"buy <userID> <goodID> <goodCounter>
	    			System.out.println("we in");
	    			String rs=  this.transferGood(user,message[1],message[2],command.getSig(),command.buyerSignature());//seller, buyer, goodID
		    		if(!rs.equals(NOK)) {
		    			//eIDLib eid = new eIDLib();
		    			System.out.println("okay, writing cert");
		    			int days = 7;
						X509Certificate cert = null;
			    		//cert = eid.getCert();
		    			//cert= null;
			    		//eid.sign(cert,rs);
						String mess = ACK ;

						sigs = new signature[3];
			    		Recorded rec = new Recorded("", counters.get(message[2]), Integer.parseInt(ts));

//<<<<<<< HEAD
//			    		
//			    		Message mes = new Message(this.idNotary, mess, sigs, rec,cert);
//			    		
//			    		mes.setSignature(
//			    				new signature(
//			    						PKI.sign(mes.getHash(), idNotary, PASS), mes.getHash())
//			    				);
//			    		return mes;
//=======
			    		Message response = new Message(this.idNotary, mess, sigs, rec,cert);
			    		response.setSignature(new signature(PKI.sign(response.getHash(),idNotary,PASS),response.getHash()));
			    		return response;
		    		}else
		    			error = "notvalidtransfer "+ts;
		    		sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);
		    		Recorded rec = new Recorded("", -1, -1);
		    		
		    		result =  new Message(this.idNotary, error,sigs,rec,null);
		    		//notario assina totalidade da mensagem
		    		result.setSignature(
		    				new signature(
		    						PKI.sign(result.getHash(), idNotary, PASS), result.getHash())
		    				);
		    		return result;

		    		//return new Message(this.idNotary, error,sigs, rec,null);
	    		}else
	    			error = "wrongcounter "+command.getRec().getCounter();
	    		sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);
	    		Recorded rec = new Recorded("", 0, Integer.parseInt(ts));

	    		result =  new Message(this.idNotary, error,sigs,rec,null);
	    		//notario assina totalidade da mensagem
	    		result.setSignature(
	    				new signature(
	    						PKI.sign(result.getHash(), idNotary, PASS), result.getHash())
	    				);
	    		return result;

	    		//return new Message(this.idNotary, error,sigs, rec,null);
	    	}else
	    		error = "notValidOperation";
	    		sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);
	    		Recorded rec = new Recorded("", -1, -1);

	    		result =  new Message(this.idNotary, error,sigs,rec,null);
	    		//notario assina totalidade da mensagem
	    		result.setSignature(
	    				new signature(
	    						PKI.sign(result.getHash(), idNotary, PASS), result.getHash())
	    				);
	    		return result;

	    		//return new Message(this.idNotary, error,sigs, rec,null);
		}else
			System.out.println("Signature not Valid");
			error = "signatureNotValid";
		    sigs[0]=  new signature(PKI.sign(error,idNotary,PASS), error);
    		Recorded rec = new Recorded("", 0, -1);
    		

    		result =  new Message(this.idNotary, error,sigs,rec,null);
    		//notario assina totalidade da mensagem
    		result.setSignature(
    				new signature(
    						PKI.sign(result.getHash(), idNotary, PASS), result.getHash())
    				);
    		return result;

	}
	/**
	 * Transferir o good ao user
	 * 
	 * @param goodID
	 * @return Transaction(?
	 *)
	 * @throws Exception 
	 */
	private String transferGood( String seller,String buyer , String goodID,signature sigSeller,signature sigBuyer) throws Exception {
		//for(String s: goods.keySet()) {System.out.println(s);}
		//this.updateState();
//=======
//	private String transferGood( String seller,String buyer , String goodID,byte[] sigSeller,byte[]sigBuyer) throws Exception {
//>>>>>>> c470555684bd38333b6e32f799da95dafbbc8e65
		if(goods.get(goodID).equals(seller)) {
			System.out.println("SELLER OK "+ seller);
			if(states.get(goodID).equals(GoodState.ONSALE)) {
				//String sigMsg = "intentionbuy "+goodID + " "+counters.get(goodID);
				System.out.println("verifying signature for :"+ buyer);
				if(PKI.verifySignature(sigBuyer.getData(), sigBuyer.getBytes(), buyer)){
					System.out.println("buyer intention verified");
					//store.writeLog(goodID,seller,buyer,""+counters.get(goodID),sigSeller,sigBuyer);
					store.updateFile(goodID, buyer);
					goods.replace(goodID, buyer); 
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
	
	private void startBroadCast(String msg, signature sigt) {
		System.out.println("starting broadcast");
		signature[] sigs = new signature[3];//propria write buyer
		sigs[0]=sigt;
		
		Recorded rec = new Recorded("", 0, 0);
		Message mss= new Message(this.idNotary,ECH+" "+msg, sigs,rec,null);
		signature sig;
		try {
			sig = new signature(PKI.sign(mss.getHash(), mss.getID(), PASS), mss.getHash());
			mss.setSignature(sig);

		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.sentEcho=true;
		for(String server: servers) {
			try {
				//envia mensagem de echo
				lib.sendMessage(server, mss);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private void start2ndPhase(String msg) throws InvalidKeyException, Exception{
		System.out.println("starting 2nd broadcast");
		signature[] sigs = new signature[3];//propria write buyer
		Recorded rec = new Recorded("", 0, 0);
		Message mss= new Message(this.idNotary,RDY+" "+msg, sigs,rec,null);

		signature sig = new signature(PKI.sign(mss.getHash(), mss.getID(), PASS), mss.getHash());
		mss.setSignature(sig);
		
		for(String server: servers) {
			try {
				lib.sendMessage(server, mss);

				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	synchronized void handleBroadCast(Message msg) {
		System.out.println("HANDLE BROADCAST");
		String text=msg.getText();
		String[] spl = text.split(" ");
		String cmd= spl[0];
		String uid =msg.getID();
		String req="";
		int acks=0;
		
		//
		try {
			if(PKI.verifySignature(msg.getHash(), msg.getSig().getBytes(), msg.getID())) {
				System.out.println("notary verified");
				
				for(int i=1;i<spl.length;i++ ){req+=spl[i] +" ";}

				switch(cmd) {
					case(ECH):
						if(this.echos.get(uid)=="") {
							responses++;
							System.out.println("new echo from: "+uid);
							  echos.put(uid,req);
							  //verifica consensus
							  for(String serv : echos.keySet()) {
									  if(echos.get(serv).equals(req)) {
										  //verificar a ssinatura do seller e do notario
										  msg.getSellSig() ; msg.getSig();//verificar com o pki 
										  acks++;

										  System.out.println("ack echo from: "+ serv+ " total acks: "+ acks);
										  System.out.println(acks>(N+f)/2 );
										  System.out.println(sentReady);
										  if(acks>(N+f)/2 & sentReady==false){
											  //acks=0;
											  sentReady = true;
											  System.out.println("1stphase completed");
											  /*return*/ start2ndPhase(req);
											  responses=0;
										  }
									  } 
								  
								  
							  }
							  if(responses>(N+f)/2 & acks<2f) {
									Thread[] list = new Thread[Thread.activeCount()];
									 Thread.currentThread().getThreadGroup().enumerate(list);
									 for(Thread t:list) {
										 if (t.getId()==waitID) {
											 t.interrupt();
										 }
									 }
										
									
									}

						}
						break;
					case(RDY):
						if(this.readies.get(uid)=="") {
							responses++;
							//System.out.println("heyyyy 2nd time");
							  System.out.println("new ready from: "+ uid);

							readies.put(uid, req);
							for(String serv: readies.keySet()) {
								if(readies.get(serv).equals(req)) {
									acks++;

									  System.out.println("ack ready from: "+ serv + " total acks: "+ acks);

									if(acks>f & this.sentReady==false) {
										start2ndPhase(req);						}
									if(acks>2f & this.delivered==false) {
										responses=0;
										acks=0;
										delivered=true;
										notifyAll();
										this.resetBRB();
										break;
										//System.out.println(Thread.activeCount());
										//return to user
									}
								}
							}
							if(responses>(N+f)/2 & acks<2f) {
								System.out.println("no no no");
								Thread[] list = new Thread[Thread.activeCount()];
								 Thread.currentThread().getThreadGroup().enumerate(list);
								 for(Thread t:list) {
									 if (t.getId()==waitID) {
										 resetBRB();
										 t.interrupt();
									 }
								 }
									
								
								}
						}
					break;
						
					default:
						break;
					
					
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
