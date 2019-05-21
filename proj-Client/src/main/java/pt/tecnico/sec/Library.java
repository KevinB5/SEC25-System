package pt.tecnico.sec;

import java.io.BufferedReader;
import pteidlib.PTEID_ADDR;
import pteidlib.PTEID_Certif;
import pteidlib.PTEID_ID;
import pteidlib.PTEID_PIC;
import pteidlib.PTEID_Pin;
import pteidlib.PTEID_TokenInfo;
import pteidlib.PteidException;
import pteidlib.pteid;

import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.CK_SESSION_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
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
    
    


	private HashMap<String,Boolean> acklist= new HashMap<String,Boolean>();
	private HashMap<String,Recorded> readlist = new HashMap<String,Recorded>();
	
	private HashMap<String,RecordSig> signaturelist = new HashMap<String,RecordSig>();
	private HashMap<String,RecordSig> writesignaturelist = new HashMap<String,RecordSig>();
	private HashMap<String, RecordSig> buyersignaturelist = new HashMap<String,RecordSig>();
	private HashMap<String, RecordString> textlist = new HashMap<String,RecordString>();
	private HashMap<String,RecordSig> certlist = new HashMap<String,RecordSig>();
	boolean citizencard =false;
	
	static String hashLimit ="0000";
    
    public Library(User user, String _ip, HashMap<String, Integer> servPorts, boolean citizencard) {
    	this.ip =_ip;
    	this.connectServer(_ip, servPorts);
    	this.idUser = user.getID();
    	this.PORT = user.gtPort();
    	this.user = user;
    	this.citizencard=citizencard;
    	
    }
    

    class RecordString {
    	  String s;
    	  int timestamp;
    	  RecordString(String s, int t) {this.s=s;this.timestamp=t;}
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
//			System.out.println("verifying answer");
		
			///System.out.println(res.getSig().getBytes());
			
			

			
			if(res ==null) {
				System.out.println("so sad");
				return "NOT OK";}	
			int ts=res.getRec().getTS();

				
			if(!(res.getCertSig()==null))
				certlist.put(serv,new RecordSig(res.getCertSig(),ts));
			//System.out.println("message from notary: "+res.getText());
			//int ts=res.getRec().getTS();
//			System.out.println("verifying answer");
//			System.out.println(ts + " " + wts);
//
//	
//			System.out.println(res.getSig().getBytes());
//			System.out.println(PKI.verifySignature(res.getHash(),res.getSig().getBytes(),res.getID()));
//			System.out.println(res);
//			System.out.println(ts+" "+wts);
			
			if(PKI.verifySignature(res.getHash(),res.getSig().getBytes(),res.getID())
					&& res.getText().split(" ")[0].equals("ACK") 
					&& ts==wts) {
//				System.out.println("YAYYYY");
				this.acklist.put(serv, true);
				acks++;
				System.out.println(acks);
				if(acks> (n+f)/2) {
					System.out.println("Achieved Quorum of Acks");
					signature maxSig = maxSig(certlist);
					System.out.println("Certificate Received, signature:\n"+maxSig);
					if(citizencard && (maxSig!=null)) {
						eIDLib eid = new eIDLib();
						X509Certificate cert = eid.getCert();				
						System.out.println("Certificate valid:\n"+ eid.verifySignature(res.getCertSig().getBytes(),res.getText()));
						
					}
					
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
//		System.out.println("Sending ReadRequest...");
		clearReadList();
		int reads=0;
		Message res=null;
		String msg=null;
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
				String state = split[2].toLowerCase() ;
				String challnge = split[3];
//				String[] writeText = res.getWriteSignature().getData().split(" ");
				
				int counter = rec.getCounter();
				int ts = rec.getTS();
								
//				System.out.println("owner,state,challenge,counter,ts,r\n");
//				System.out.println(owner+"//"+state+"//"+challnge+"//"+counter+"//"+ts+"//"+split[4]);
				
				
				int r = Integer.parseInt(split[4]);

				boolean writerVerified = false;
				
				
				
//				System.out.println(state + " "+ counter + " "+ts);
				
				
				/* checks if signature of writer is okay -- contains special case for counter = ts = 0  */
				if(state.equals("notonsale") && counter==0 && ts ==0)
					writerVerified = true;
				else{
					if(state.equals("onsale")) {
					msg ="sell " +good + " "+(counter-1)+" "+ts;
//					System.out.println("testing with: "+msg);
					writerVerified = PKI.verifySignature(msg, res.getWriteSignature().getBytes(), owner);


					}else {
//						ts = Integer.parseInt(split[split.length-3]);
						msg ="transfer " +owner +" "+ good + " "+(counter-1)+" "+ts;
//						System.out.println("testing with: "+msg);
						writerVerified = PKI.verifySignature(msg, res.getWriteSignature().getBytes(), split[5]);
						
					}
					
				}
//				signature[] sigs = new signature[3];
//				sigs[0]= new signature(res.getWriteSignature().getBytes(), wb);	

//				System.out.println(res.getSig());
//				
//				System.out.println(res.getSig().getData());
//				System.out.println(res.getHash());
				
				byte[] hash = res.getHash();
				
				
				if(PKI.verifySignature(hash,res.getSig().getBytes(),res.getID())
						&& r==rid
						&& split[3].equals(challenge)) {
//					System.out.println("heyyy");
					
					
					///// nao é o writer q está verified mas sim o notario///////

//				System.out.println("Writer Verified: "+writerVerified);
				
				
					
				////// a verificar duas vezes a assinatura do notrio //////////
				
				if(PKI.verifySignature(res.getHash(),res.getSig().getBytes(),res.getID())
					&& r==rid
					&& challnge.equals(challenge)
					&& writerVerified) {


						writesignaturelist.put(serv, new RecordSig(res.getWriteSignature(),ts));
						textlist.put(serv,new RecordString(res.getText(),ts));
						readlist.put(serv,res.getRec());
						buyersignaturelist.put(serv, new RecordSig(res.buyerSignature(),ts));
						reads++;
						
					if(reads > (n+f)/2) {
						System.out.println("Achieved Byzantine Quorum. Doing write-back...");
						/* prepare message for write-back */
						
						Recorded WBRec = maximumValue(readlist);
						signature maxBuySig = maxSig(buyersignaturelist);
						signature maxWriteSig = maxSig(writesignaturelist);

						
						int maxts = WBRec.getTS();
						
						if(maxts==0) {
//							System.out.println("ZERO COUNTER");
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

						signature[] WBSig = new signature[3];
						WBSig[1]= maxWriteSig;
						WBSig[2]=maxBuySig;
						
						Message writeBack = null;			
						/* Creating winner message */
						if(maxstate.toLowerCase().equals("onsale")) {

							// message was "sell goodID"
							wb="sell "+good+ " " +(maxcounter-1)+" "+ maxts;
							writeBack = new Message(maxowner, wb, WBSig,  WBRec, null,powHash(wb));
						}else {

							wb= "transfer "+maxowner+" "+good+" "+ (maxcounter-1) +" "+ maxts;
							WBRec= new Recorded("",maxcounter-1,maxts);
//							System.out.println("sending: "+wb);
							writeBack = new Message(split[5], wb, WBSig,  WBRec, null,powHash(wb));
						}
						
						
						
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
	

	private String maxString(HashMap<String, RecordString> statelist2) {
		int max = 0;
		String ret ="";
		//TODO: maxsig not being returned!
		for(String serv : statelist2.keySet()) {
			int ts = statelist2.get(serv).timestamp;
			if(ts>=max) {
				ret = statelist2.get(serv).s;
				max = ts;
			}
		}
		return ret;		
	}
	
	
	private signature maxSig( HashMap<String,RecordSig> siglist) {
		int max = 0;
		signature maxsig = null;
		for(String serv : siglist.keySet()) {
			maxsig=siglist.get(serv).sig;
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
//		 			System.out.println(temp);
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
	
	public static String powHash(String content) {
		/* Returns string i such that content+i hashes to a string with hashLimit in the beginning */
		MessageDigest digest=null;
		try {
			 digest = MessageDigest.getInstance("SHA-1");
		 } catch (NoSuchAlgorithmException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		digest.reset();
//		System.out.println("POWHashing: "+content);
		String originalContent = content;
		byte[] hash = null;
		String hashString= null;
		int i=0;
		do {
			i++;
			hashString = originalContent+i;
//			System.out.println(hashString);
			hash = digest.digest(hashString.getBytes(StandardCharsets.UTF_8));
			content = DatatypeConverter.printHexBinary(hash);
			hashString = content.substring(0,4);
//				System.out.println("hashString: "+hashString);
		}
		while(!hashString.equals(hashLimit)); 
//		System.out.println(i);
		return ""+i;
	}
}
