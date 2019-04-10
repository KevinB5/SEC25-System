package pt.tecnico.sec;

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
	private String idUser ;
	
	private HashMap<String,GoodState> goods = new HashMap<String,GoodState>();
	private HashMap<String,String> counters = new HashMap<String,String>();
	private HashMap<String,String> usrPorts = new HashMap<String,String>();
	private HashMap<String,byte[]> buyersigs = new HashMap<String,byte[]>();
	private ArrayList<String> allUsers = new ArrayList<String>();
	private Library lib;
	private static final String OK ="OK";
	private static final String NOK ="Not OK";
	private String ip;
	private static String line = System.getProperty("file.separator");
	private static final String path2 = originPath()+ line + "ports.txt";

	private static ServerSocket serverSocket=null;
    private static int PORT;
	private int keySize ;
	private static String PASS;
	public static KeyStore KEYSTORE;
	public PublicKey notarypublickey;
	private String challenge;




	
	/**
	 * Conectar um cliente ao servidor (notary)
	
	public static void main(String[] args) {
		
	} */
	
	/**
	 * Informar ao notary que quer vender um good
	 * @throws Exception 
	 */
	
	public User(String id, String ip, int Svport) throws Exception {
		
		idUser = id;
		this.ip=ip;
		
		this.getPort();
		
		if(allUsers.contains(idUser)) {
		
			lib = new Library(this, ip, Svport);
			Storage store = new Storage();
			
			ArrayList<String> res =store.getGoods(id);
			for(String good : store.getGoods(id)) {
				goods.put(good, GoodState.NOTONSALE);
			};
			printgoods();
			
			Random random = new Random();	
			
			int rnd = random.nextInt();
			PASS = idUser + rnd;
			
			X509Certificate cert = this.createKeys(id, PASS);
			PKI.getInstance();
			PKI.setKey(idUser, cert);
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
	
	byte[] sign(String data) throws InvalidKeyException, Exception{
		Signature rsa = Signature.getInstance("SHA1withRSA"); 
		rsa.initSign(this.getKey(PASS));
		rsa.update(data.getBytes());
		//System.out.println("Signing " + data);
		return rsa.sign();
	}
	
	 boolean verifySignature(byte[] data, byte[] signature, String uID) throws Exception {
		Signature sig = Signature.getInstance("SHA1withRSA");
		PKI.getInstance();
		//System.out.println(lib.getKey(uID));
		sig.initVerify(PKI.getKey(uID));
		sig.update(data);
		
		return sig.verify(signature);
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
						System.out.println("USER: "+line.replace("#",""));
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

    	if(op.equals("sell")) {
    		if(res.length!=2) {
    			System.out.println("correct syntax: sell <goodID>");
    		}else {
    		this.getStateOfGoodInvisible(res[1]);
    		this.intentionToSell(res[1]);
    		}
    	}else
    	
    	if(op.equals("state"))
    		if(res.length!=2) {
    			System.out.println("correct syntax: state <goodID>");
    		}else {
        		this.getStateOfGood(res[1]);
    		}
    	
    	if(op.equals("buy"))
    		if(res.length!=3) {
    			System.out.println("correct syntax: buy <sellerID> <goodID>");
    		}else {
    			//this.getStateOfGoodInvisible(res[1]);
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
			res = lib.intentionToSell(idUser, good, counters.get(good));
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
	
	private void getStateOfGood(String good) {
		try {
			//Random string of 20 lowercase characters
			challenge = generateRandomString(20);
			//update counter
			counters.put(good,lib.getStateOfGood(good,challenge));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getStateOfGoodInvisible(String good) {
		try {
			//Random string of 20 lowercase characters
			challenge = generateRandomString(20);
			//update counter
			counters.put(good,lib.getStateOfGoodInvisible(good,challenge));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Informar ao notary que quer comprar um dado good
	 * 
	 * @param good
	 * @param  
	 */
	private void buyGood (String user, String good) {
		String counter = counters.get(good);
//		for(String c : counters.values())
//			System.out.println(c);
		if(counter==null) {
			System.out.println("Need to verify state first");
			return;
		}
		String res = "";
		try {
			lib.buyGood(user, good,counter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(res.equals(OK)) {
			goods.put(good, GoodState.NOTONSALE);
			printgoods();
		}
	}
	
	/**
	 * Transferir um dado good
	 * 
	 * @param good
	 */
	private void transferGood(String buyer, String good) {
		String counter=counters.get(good);
		String res= "";
		try {
			res = lib.transferGood(idUser, buyer, good, counter, buyersigs.get(buyer));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(res.equals(OK)) {
			sellGood(good);
			goods.remove(good);
			printgoods();
		}
		
	}
	
	private void sellGood(String goodID) {
		System.out.println("selling "+ goodID);
	}
	
	/**
	 * Transferir a informacao da compra
	 */
	private void transferBuyInfo() {
		
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
	
	
	public String execute(Message command) throws Exception {
		//"correct syntax: buy <sellerID> <goodID>"

		String msg = command.getText();
    	String [] res = msg.split(" ");
    	if(res.length<2) {
    		System.out.println("Operation not valid: missing arguments");
    		return "";
    	}
    	String op =  res[0];
    	String error = "Not valid";
    	if(command.getID()=="notary") {
    		//if notary is sending second key we update
    		if(command.getText()=="key2") {
    			//TODO: meter condição para verificar se foi assinado com a chave do CC
    			notarypublickey=(PublicKey) command.getObj();
    		}else
    		if(this.verifySignatureNotary(msg.getBytes(), command.getSig()));
    	}else
    	if(!this.verifySignature(msg.getBytes(), command.getSig(), command.getID()))
    		return error;
    	
    	if(op.equals("intentionbuy")) {//buy buyerID goodID
    		String ret = "no such good";
    		System.out.println("trying to buy "+ res[1]);
    		if(goods.containsKey(res[1])) {
    			ret=lib.transferGood(this.idUser, command.getID(), res[1],counters.get(res[1]), command.getSig());
	    		if(ret.equals("OK"))
	    			goods.remove(res[1], goods.get(res[1]));
	    			lib.sendMessage(command.getID(),new Message(idUser, ret, sign(ret), null, null, null));
	    		this.printgoods();
	    		}
    		return ret;
    	}

    	else
    		return "no valid operation " + command ;
	}
	
	private boolean verifySignatureNotary(byte[] bytes, byte[] sig) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");  
	    cipher.init(Cipher.DECRYPT_MODE, notarypublickey);
	    return (bytes==cipher.doFinal(sig));
	}

	private PrivateKey getKey(String password) {//userID, password
		
	    KeyStore.ProtectionParameter protParam =
	            new KeyStore.PasswordProtection(password.toCharArray());
		
	    KeyStore.PrivateKeyEntry pkEntry;
	    PrivateKey myPrivateKey = null ;
		try {
			/*pkEntry = (KeyStore.PrivateKeyEntry)
			        KEYSTORE.getEntry(id , protParam);*/
			
					myPrivateKey = (PrivateKey) KEYSTORE.getKey(this.idUser, password.toCharArray());

		} catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
			e.printStackTrace();
		}
		System.out.println("handing out private key ");
	        return myPrivateKey;
		
	}

	private X509Certificate createKeys(String userID, String word) {
	    KeyPairGenerator keyGen;
	    KeyPair keyPair = null;
	    PublicKey pubKey = null;
	    char [] pwdArray = "password".toCharArray();
	    X509Certificate cert = null;
		try {
			
			this.KEYSTORE = KeyStore.getInstance(KeyStore.getDefaultType());
			
			
			KEYSTORE.load(null, pwdArray);
			
			try(FileOutputStream fos = new FileOutputStream("newKeyStoreFileName.jks")) {
			    KEYSTORE.store(fos, pwdArray);
			}
			
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);

			keyPair = keyGen.generateKeyPair();
						
			pubKey = keyPair.getPublic();
			//KEYS.put(userID,pubKey);
			
			System.out.println("saving Public key for "+ userID);
			
			
			char[] password = word.toCharArray();
			 
			cert = this.generateCertificate(userID, keyPair, 0, "SHA1withRSA");
			
			//guardar o certificado na keystore para poder obter a pubkey
			
			
			
			
			
			Certificate[] chain = {cert};
			
			  
			
			KeyStore.PrivateKeyEntry skEntry = new KeyStore.PrivateKeyEntry((PrivateKey) keyPair.getPrivate(), chain);
			KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(password);

			KEYSTORE.setEntry(userID,skEntry,protParam);
		//KEYSTORE.setKeyEntry(userID,(PrivateKey) keyPair.getPrivate(), password, null);
					
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return cert;
	}
	
	private X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm)
			  throws GeneralSecurityException, IOException
			{
			  PrivateKey privkey = pair.getPrivate();
			  X509CertInfo info = new X509CertInfo();
			  Date from = new Date();
			  Date to = new Date(from.getTime() + days * 86400000l);
			  CertificateValidity interval = new CertificateValidity(from, to);
			  BigInteger sn = new BigInteger(64, new SecureRandom());
			  X500Name owner = new X500Name("CN="+dn);
			 
			  info.set(X509CertInfo.VALIDITY, interval);
			  info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
			  info.set(X509CertInfo.SUBJECT, owner);
			  info.set(X509CertInfo.ISSUER,owner);
			  info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
			  info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
			  AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
			  info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
			 
			  // Sign the cert to identify the algorithm that's used.
			  X509CertImpl cert = new X509CertImpl(info);
			  cert.sign(privkey, algorithm);
			 
			  // Update the algorith, and resign.
			  algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
			  info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
			  cert = new X509CertImpl(info);
			  cert.sign(privkey, algorithm);
			  return cert;
			}   

}



