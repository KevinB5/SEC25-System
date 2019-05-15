package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.DatatypeConverter;

public class Library {
	private ArrayList<Socket> servConnects = new ArrayList<Socket>();
	private ServerSocket serverSocket;

	private int n;
	private int f=1;
    private HashMap<String,ObjectOutputStream> out = new HashMap<String,ObjectOutputStream>();
    private HashMap<String,ObjectInputStream> in = new HashMap<String,ObjectInputStream>();
    
    private ObjectOutputStream outU;
    private ObjectInputStream inU;
    private String ip;
	static int acks=0;

    private final String idUser;   
	private HashMap <String, Socket> sockets = new HashMap<String, Socket>();
	private static int PORT;
    private User user;
    
    private final String hashLimit = "0000";


	private HashMap<String,Boolean> acklist= new HashMap<String,Boolean>();
	private HashMap<String,Recorded> readlist = new HashMap<String,Recorded>();
	
	private HashMap<String,RecordSig> signaturelist = new HashMap<String,RecordSig>();
	private HashMap<String,RecordSig> writesignaturelist = new HashMap<String,RecordSig>();
	private HashMap<String,RecordCert> certlist = new HashMap<String,RecordCert>();
	
    
    public Library(User user, String _ip, HashMap<String, Integer> servPorts) {
    	this.ip =_ip;
    	this.connectServer(_ip, servPorts);
    	this.idUser = user.getID();
    	this.PORT = user.gtPort();
    	this.user = user;
    
    	
    }
    
    
    class RecordCert {
    	  X509Certificate cert;
    	  int timestamp;
    	  RecordCert(X509Certificate c, int t) {this.cert=c;this.timestamp=t;}
    	}
    

    class RecordSig {
  	  signature sig;
  	  int timestamp;
  	  RecordSig(signature s, int t) {this.sig=s;this.timestamp=t;}
  	}
    
	public void connectServer(String ip, HashMap<String, Integer>servPorts) {
		for(int port : servPorts.values()) {
			try {
				
				Thread.sleep(500);
				Socket servConnect = new Socket(ip, port);
				System.out.println("connected to server at port: "+ port);
				String sId="";
				for(String id : servPorts.keySet()) {
					if(servPorts.get(id).equals(port))
						sId = id;
				}
					
	            out.put(sId, new ObjectOutputStream(servConnect.getOutputStream())); 
	            in.put(sId, new ObjectInputStream(servConnect.getInputStream()));
	            n++;
	//    			System.out.println("all good");
	            servConnects.add(servConnect);
	          
			
			}catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
    	
	}
    
    public void connectUser( String Uip,String userID, int Uport) {
		try {
			System.out.println("connecting to "+userID+" in port " + Uport+"...");

			Socket clientSocket = new Socket(Uip, Uport);
			this.sockets.put(userID, clientSocket);
			
		}catch(ConnectException cnn) {
			System.out.println(userID + " is not connected");
			System.out.println(cnn.getMessage());
		}catch(IOException ie) {
			ie.printStackTrace();
		}
	}
    

	public String write(String serv,Message intent, int wts) throws Exception {
		clearAcklist();
		Message res = null;
		ObjectOutputStream ouSt;
		ObjectInputStream inSt;
		//for(String serv : out.keySet()) {
		try {
			ouSt = out.get(serv);
			inSt = in.get(serv);
			ouSt.writeObject(intent);
			res = (Message)inSt.readObject();
			//System.out.println("message from notary: "+res.getText());
			Thread.sleep(1000*2);
			//int ts=res.getRec().getTS();
			System.out.println("verifying answer");
		
			///System.out.println(res.getSig().getBytes());
			
			

			
			if(res ==null) {
				System.out.println("so sad");
				return "NOT OK";}	
			int ts=res.getRec().getTS();

				
			if(!(res.getCertSig()==null))
				certlist.put(serv,new RecordCert(res.getCertSig(),ts));
			//System.out.println("message from notary: "+res.getText());
			//int ts=res.getRec().getTS();
			System.out.println("verifying answer");
			System.out.println(ts + " " + wts);

	
			System.out.println(res.getSig().getBytes());
			if(PKI.verifySignature(res.getHash(),res.getSig().getBytes(),res.getID())
					&& res.getText().split(" ")[0].equals("ACK") 
					&& ts==wts) {
				System.out.println("YAYYYY");
				this.acklist.put(serv, true);
				acks++;
				System.out.println(acks);
				if(acks> (n+f)/2) {
					System.out.println("Achieved Quorum of Acks");
					X509Certificate maxcert= maxCert(certlist);
					System.out.println("Certificate Received:\n"+maxcert);
					acks=0;
					return "OK";
				}
			}
		}
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//}
		return "NOT OK";	
	}
	
	public Message read(Message intent, int rid, String challenge, String good) throws Exception {
		System.out.println("Sending ReadRequest...");
		clearReadList();
		int reads=0;
		Message res=null;
		ObjectOutputStream ouSt;
		ObjectInputStream inSt;

		System.out.println("sending: "+intent.getText());
		System.out.println(out.keySet());
		
		for(String serv : out.keySet()) {
			try {
				ouSt = out.get(serv);
				inSt = in.get(serv);
				ouSt.writeObject(intent);
				res = (Message)inSt.readObject();
				
				System.out.println("text from notary: "+res.getText());
				
				String[] split =res.getText().split(" ");
				
				
				Recorded rec = res.getRec();
				
				/* Values for comparison */
				String owner = split[1];
				String state = split[2];
				String challnge = split[3];
				
				int counter = rec.getCounter();
				int ts = rec.getTS();
								
				System.out.println("owner,state,challenge,counter,ts,r\n");
				System.out.println(owner+"//"+state+"//"+challnge+"//"+counter+"//"+ts+"//"+split[4]);
				
				
				int r = Integer.parseInt(split[4]);

				boolean writerVerified = false;
				
				
				
				System.out.println(state + " "+ counter + " "+ts);
				
				
				/* checks if signature of writer is okay -- contains special case for counter = ts = 0  */
				if(state.equals("NOTONSALE") && counter==0 && ts ==0)
					writerVerified = true;
				else{
					if(state.equals("ONSALE")) {
					String msg ="sell " +good + " "+(counter-1)+" "+ts;
					System.out.println("testing with: "+msg);
					writerVerified = PKI.verifySignature(msg, res.getWriteSignature().getBytes(), owner);


					}else {
						String msg ="owner " +good + " "+(counter-1)+" "+ts;
						System.out.println("testing with: "+msg);
						writerVerified = PKI.verifySignature(msg, res.getWriteSignature().getBytes(), owner);
					}
					
				}
//				signature[] sigs = new signature[3];
//				sigs[0]= new signature(res.getWriteSignature().getBytes(), wb);	
				byte[] hash = res.getHash();
				
				if(PKI.verifySignature(hash,res.getSig().getBytes(),res.getID())
						&& r==rid
						&& split[3].equals(challenge)) {
					System.out.println("heyyy");

				System.out.println("Writer Verified: "+writerVerified);
				
				if(writerVerified)
					writesignaturelist.put(serv, new RecordSig(res.getWriteSignature(),ts));

				
				if(PKI.verifySignature(res.getHash(),res.getSig().getBytes(),res.getID())
					&& r==rid
					&& challnge.equals(challenge)
					&& writerVerified) {


						
						readlist.put(serv,res.getRec());
						signaturelist.put(serv, new RecordSig(res.buyerSignature(),ts));
						reads++;
						
					if(reads > (n+f)/2) {
						System.out.println("Achieved Byzantine Quorum. Doing write-back...");
						/* prepare message for write-back */
						
						Recorded WBRec = maximumValue(readlist);
						signature maxSig = maxSig(signaturelist);
						signature maxWriteSig = maxSig(writesignaturelist);
						
						int maxts = WBRec.getTS();
						
						if(maxts==0) {
							System.out.println("ZERO COUNTER");
							signature[] zcsig = new signature[3];
							Message zerocounter = new Message(owner,"zerocounter",zcsig,WBRec,null);
//							System.out.println("message" + zerocounter.getText());
							return zerocounter;
						}

						int maxcounter = WBRec.getCounter(); 
						String[] maxownerstate = WBRec.getState().split(" ");
						String maxstate = maxownerstate[1];
						String maxowner = maxownerstate[0];
						String wb = null; //Write-Back message

						if(maxstate.equals("ONSALE")) {

							// message was "sell goodID"
							wb="sell "+good+ " " +maxcounter+" "+ maxts;
						}else {

							// message was "owner goodID"
							wb= "owner "+good+" "+ maxcounter +" "+ maxts;
						}
						signature[] WBSig = new signature[3];
						
						
						WBSig[1]= maxWriteSig;
						Message writeBack = new Message(maxowner, wb, WBSig,  WBRec, null);			
						
						return writeBack;
					}
				}
				}
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		}
		return null;
	}
	
	
	private X509Certificate maxCert(HashMap<String,RecordCert> certlist2) {
		int max=0;
		X509Certificate maxcert = null;
		for(String serv : certlist2.keySet()) {
			int ts = certlist2.get(serv).timestamp;
			if(ts>=max) {
				maxcert=certlist2.get(serv).cert;
			}
		}
		return maxcert;
		
		
	/*	return "NOT OK";*/
	}
	
	
	
	private Recorded maximumValue(HashMap<String, Recorded> statelist2) {
		int max = 0;
		String maxstate=null;
		int maxcounter= 0;
		Recorded ret = null;
		//TODO: maxsig not being returned!
		for(String serv : statelist2.keySet()) {
			int ts = statelist2.get(serv).timestamp;
			if(ts>=max) {
				maxstate=statelist2.get(serv).getState();
				int mr=statelist2.get(serv).getCounter();
				maxcounter = mr;
				max = ts;
			}
		}
		ret = new Recorded(maxstate,maxcounter,max);

		return ret;		
	}
	
	
	private signature maxSig( HashMap<String,RecordSig> siglist) {
		int max = 0;
		signature maxsig = null;
		for(String serv : siglist.keySet()) {
			if(siglist.get(serv).timestamp>=max) {
				
				maxsig=siglist.get(serv).sig;
			}
		}
		return maxsig;
	}
	
	private void clearAcklist() {
		for(String sv : acklist.keySet()) {
			this.acklist.replace(sv, false);
		}
	}
	
	
	private void clearReadList() {
		readlist = new HashMap<String,Recorded>();
		signaturelist = new HashMap<String,RecordSig>();
		writesignaturelist = new HashMap<String,RecordSig>();
	}
	

	public Message sendMessage(String uID, Message msg) throws Exception {
		 ObjectOutputStream printer= null;
		 ObjectInputStream reader=null;
		 Message resp= null;
		 try {
//			 System.out.println("Sending message to "+ uID);
		 	
			 Socket clientSocket = sockets.get(uID);
			 if(clientSocket == null)
				 throw new Exception("Must connect to user "+ uID+" first");
				outU = new ObjectOutputStream(clientSocket.getOutputStream());
				inU= new ObjectInputStream(clientSocket.getInputStream());
		 		
		 		outU.writeObject(msg);

		 		Message temp = (Message) inU.readObject();
		 		if (temp.getClass().equals(String.class)) {
		 			System.out.println(temp);
		 			return null;
		 		}
		 		else {
		 			resp = (Message) temp;
		 		}
				//System.out.println("inU.readObject() " + inU.readObject());
				return resp;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(NullPointerException ne) {
				System.out.println(ne.getMessage());
			
			
			}catch(Exception e) {
				e.printStackTrace();
			}

	        return resp;

	    }
	
	public String powHash(String content) {
		/* Returns string i such that content+i hashes to a string with hashLimit in the beginning */
		MessageDigest digest;
		byte[] hash = null;
		String hashString= null;
		int i=0;
		do {
			hashString = content+i;
			try {
				digest = MessageDigest.getInstance("SHA-256");
				hash = digest.digest(hashString.getBytes(StandardCharsets.UTF_8));
				hashString = DatatypeConverter.printHexBinary(hash);
				hashString = hashString.substring(0,4);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			i++;
		}
		while(!hashString.equals(hashLimit)); 
		return ""+i;
	}
}
