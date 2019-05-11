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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

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
    private final String idUser;   
	//private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\ports.txt";
	private HashMap <String, Socket> sockets = new HashMap<String, Socket>();
	//private HashMap <String, ObjectInputStream> readers = new HashMap<String, ObjectInputStream>();
	private static int PORT;
    private User user;
    
    private final String hashLimit = "0000";
	//Byzantine
	private HashMap<String,Boolean> acklist= new HashMap<String,Boolean>();
	private HashMap<String,Recorded> readlist = new HashMap<String,Recorded>();
//	private HashMap<String,Pair> statelist = new HashMap<String,Pair>();
//	private HashMap<String,Pair> counterlist = new HashMap<String,Pair>();
	private HashMap<String,RecordSig> signaturelist = new HashMap<String,RecordSig>();
	
   // private PKI pki = new PKI(PKI.KEYSIZE);
    
    /*
     * HashMap that returns the most recent buyer signature from buyerID with an IntentToBuy goodID 
     *  from the pair <BuyerID,GoodID>
     */
    //private HashMap<Pair<String,String>, byte[]> buyerSigs = new HashMap<Pair<String,String>,byte[]>();
   
    
    public Library(User user, String _ip, HashMap<String, Integer> servPorts) {
    	this.ip =_ip;
    	this.connectServer(_ip, servPorts);
    	this.idUser = user.getID();
    	this.PORT = user.gtPort();
    	this.user = user;
    
    	
    }
    

    class RecordSig {
  	  byte[] sig;
  	  int timestamp;
  	  RecordSig(byte[] s, int t) {this.sig=s;this.timestamp=t;}
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
			System.out.println("connecting to "+userID+" in port " + Uport);

			Socket clientSocket = new Socket(Uip, Uport);
//			System.out.println("connected to server at port: "+ Uport);
			this.sockets.put(userID, clientSocket);
			
		}catch(ConnectException cnn) {
			System.out.println(userID + " is not connected");
			System.out.println(cnn.getMessage());
		}catch(IOException ie) {
			ie.printStackTrace();
		}
	}
    
/*
   public PublicKey getKey(String uID) throws InvalidKeyException, Exception {
	   String ms = "getKey uID";
	   Message msg = new Message(idUser,ms ,user.sign(ms), null, null, null );
	   
	   Message ret = send(msg);
	   return (PublicKey) ret.getObj();
   }
  */
    /*
    public PublicKey getKey(String uID) throws InvalidKeyException, Exception {
    	return PKI.getKey(uID);
    }
    */
//    public void updateBuyerSigs(String buyerID, String goodID, byte[] buyersignature) {
//    	buyerSigs.put(new Pair<String,String>(buyerID,goodID),buyersignature );
//    }
    
    /*
     * Returns the correspondent most recent Buyer Signature
     *//*
   public byte[] getSig(String buyerID, String goodID) {
		return buyerSigs.get(new Pair<String,String>(buyerID,goodID));
	}

*/
/*
public Message sendKey(PublicKey key) throws InvalidKeyException, Exception {
	   Message epa =send(new Message(this.idUser, "StoreKey",user.sign("StoreKey"),null, key, null));
	   return epa;
   }

public PublicKey getKey(String uid) throws InvalidKeyException, Exception {
	String msg = "Get "+uid;
	Message result = send(new Message(this.idUser, msg,user.sign(msg) , null, null, null ));
	return (PublicKey) result.getObj();
}

*/
	

	
/****Connection to the server ****/////	
    
	public Message sendDeprecated(Message intent) throws Exception {
		if(intent.getSig().equals(null))
			throw new Exception("Must sign message first");
		Message res = null;
		ObjectOutputStream ouSt;
		ObjectInputStream inSt;
		for(String serv : out.keySet()) {
			try {
				ouSt = out.get(serv);
				inSt = in.get(serv);
				ouSt.writeObject(intent);
				res = (Message)inSt.readObject();
				if(!PKI.verifySignature(res.getText(),res.getSig().getBytes(),res.getID())) {
					throw new Exception("Invalid message signature");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;	
	}
    
	
	public String write(Message intent, int wts) throws Exception {
		clearAcklist();
		int acks=0;
		Message res = null;
		ObjectOutputStream ouSt;
		ObjectInputStream inSt;
		for(String serv : out.keySet()) {
		try {
			ouSt = out.get(serv);
			inSt = in.get(serv);
			ouSt.writeObject(intent);
			res = (Message)inSt.readObject();
			System.out.println("message from notary: "+res.getText());
			int ts=res.getRec().getTS();
			System.out.println("timestamps: "+ts + " " + wts);
			if(PKI.verifySignature(res.getText(),res.getSig().getBytes(),res.getID())
					&& res.getText().split(" ")[0].equals("ACK") 
					&& ts==wts) {
				this.acklist.put(serv, true);
				acks++;
				if(acks> (n+f)/2) {
					return "OK";
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return "NOT OK";	
	}
	
	public String read(Message intent, int rid, String challenge, String good) throws Exception {
		clearReadList();
		int reads=0;
		Message res=null;
		ObjectOutputStream ouSt;
		ObjectInputStream inSt;
		System.out.println("sending: "+intent.getText());
		System.out.println(out.keySet());
		for(String serv : out.keySet()) {
			try {
				System.out.println("reaching "+serv);
				System.out.println("n,f: "+n+" "+f);
				ouSt = out.get(serv);
				inSt = in.get(serv);
				ouSt.writeObject(intent);
				res = (Message)inSt.readObject();
				String[] split =res.getText().split(" ");
				System.out.println("res: "+res.getText());
				int r = Integer.parseInt(split[4]);
				
				
				System.out.println("r , rid" + r + " " + rid);

				System.out.println("message from serv: "+res.getID()+", " + res.getText());
				
				String owner = split[1];
				byte[] maxsig = maxSig(signaturelist);
				/*
				if(state.equals("ONSALE")) {
					// message was "sell goodID"
					wb="sell "+good+ " " +counter+" "+ maxts;
				}else {
					// message was "owner goodID"
					wb= "owner "+good+" "+ counter +" "+ maxts;
				}
				*/
//				signature[] sigs = new signature[3];
//				sigs[0]= new signature(res.getWriteSignature().getBytes(), wb);				
				if(PKI.verifySignature(res.getText(),res.getSig().getBytes(),res.getID())
						&& r==rid
						&& split[3].equals(challenge)) {
					System.out.println("heyyy");

							int ts = res.getRec().getTS();
							System.out.println("all good");
							readlist.put(serv,res.getRec());

							signaturelist.put(serv, new RecordSig(res.getSig().getBytes(),ts));
							reads++;
					if(reads > (n+f)/2) {
					    	return split[0]+" " +maximumValue(readlist);
					}
				}
				}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		}
		return "NOT OK";
	}
	
	
	
	private String maximumValue(HashMap<String, Recorded> statelist2) {
		int max = 0;
		String maxstate=null;
		String maxcounter=null;
		byte[] maxsig;
		String ret = "";
		//TODO: maxsig not being returned!
		for(String serv : statelist2.keySet()) {
			int ts = statelist2.get(serv).timestamp;
			if(ts>=max) {
				maxstate=statelist2.get(serv).getState();
				int mr=statelist2.get(serv).getCounter();
				maxcounter = String.valueOf(mr);
				max = ts;
			}
		}
		ret+= maxstate + " "+ maxcounter + " " + max;
		return ret;		
	}
	
	
	private byte[] maxSig( HashMap<String,RecordSig> siglist) {
		int max = 0;
		byte[] maxsig = null;
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
		 		//outU.reset();
		 		//
		 		
				//
		 		//resp = (Message) inU.readObject();
				//return execRequest(resp);
				//this.stopConnectServer();
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
			//resp = pki.encrypt(,resp); falta buscar a chave privada do user
	        return resp;

	    }
	
	public String hash(String content) {
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
