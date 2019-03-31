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
	private HashMap<String,String> usrPorts = new HashMap<String,String>();
	private Library lib;
	private static final String OK ="Ok";
	private static final String NOK ="Not OK";
	private String ip;
	private static final String path2 = ".\\src\\main\\java\\pt\\tecnico\\state\\ports.txt";

	private static ServerSocket serverSocket=null;
    private static int PORT;
	private int keySize ;
	private static String PASS;
	public static KeyStore KEYSTORE;




	
	/**
	 * Conectar um cliente ao servidor (notary)
	
	public static void main(String[] args) {
		
	} */
	
	/**
	 * Informar ao notary que quer vender um good
	 */
	
	public User(String id, String ip, int Svport) {
		
		idUser = id;
		this.ip=ip;
		this.getPort();
		
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
		
		PublicKey pub = this.createKeys(id, PASS);
		lib.sendKey(pub);

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
	
	public void connectToUsers() {
		for(String key : usrPorts.keySet()) {
			lib.connectUser(ip, Integer.parseInt(usrPorts.get(key)));
		}		
	}
	
	byte[] sign(String data) throws InvalidKeyException, Exception{
		Signature rsa = Signature.getInstance("SHA1withRSA"); 
		rsa.initSign(this.getKey(PASS));
		rsa.update(data.getBytes());
		System.out.println("Signing " + data);
		return rsa.sign();
	}
	
	 boolean verifySignature(byte[] data, byte[] signature, String uID) throws Exception {
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initVerify(lib.getKey(uID));
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
					lines += " "+scnr.nextLine();	
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

    	if(op .equals("sell")) {

    		this.intentionToSell(res[1]);
    	}
    	if(op.equals("state"))
    		this.getStateOfGood(res[1]);
    	
    	if(op.equals("buy"))
    		this.buyGood(res[1], res[2]);
    	
    	if(op.equals("transfer")) {
    		if(res.length<3)
        		throw new Exception("Operation not valid: misgging arguments");
    		this.transferGood(res[1], res[2]);

    	}
    	
    	if(op.equals("connect"))
    		this.connectToUsers();
    	
    	
    	
    }
    
	private void intentionToSell(String good) {
		String res="";
		try {
			res = lib.intentionToSell(idUser, good);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(res);
		if( res.equals(OK))
			goods.replace(good, GoodState.ONSALE);
		System.out.println(goods);
		
		
	}
	
	/**
	 * Perguntar ao notary o estado de um good
	 * 
	 * @param good
	 * @return estado do good
	 */
	private void getStateOfGood(String good) {
		System.out.println(lib.getStateOfGood(good));
	}
	
	/**
	 * Informar ao notary que quer comprar um dado good
	 * 
	 * @param good
	 */
	private void buyGood (String user, String good) {
		
		String res = "";
		try {
			res = lib.buyGood(user, good);
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
	private String transferGood(String buyer, String good) {
		
		String res= lib.transferGood(idUser, buyer, good);
		
		if(res.equals(OK)) {
			sellGood(good);
			goods.remove(good);
			printgoods();
		}
		
		return res;
		
	}
	
	private void sellGood(String goodID) {
		System.out.println("selling "+ goodID);
	}
	
	/**
	 * Transferir a informacao da compra
	 */
	private void transferBuyInfo() {
		
	}
	
	public String execute(String command) throws Exception {

    	String [] res = command.split(" ");
    	if(res.length<2)
    		throw new Exception("Operation not valid: misgging arguments");
    	String op =  res[0];
    	System.out.println("trying to buy "+ res[1]+res[2]);
    	if(op .equals("intentionbuy")) {//buy buyerID goodID
    		String ret = "no such good";
    		System.out.println(res[2]);
    		if(goods.containsKey(res[2])) {
    			ret= lib.sellGood(this.idUser, res[1], res[2]);//sellerID, goodID
	    		if(ret.equals("Ok"))
	    			goods.remove(res[2], goods.get(res[2]));
	    		this.printgoods();
	    		}
    		return ret;
    	}

    	else
    		return "no valid operation " + command ;
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

	private PublicKey createKeys(String userID, String word) {
	    KeyPairGenerator keyGen;
	    KeyPair keyPair = null;
	    PublicKey pubKey = null;
	    char [] pwdArray = "password".toCharArray();
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
			 
			X509Certificate cert = this.generateCertificate(userID, keyPair, 0, "SHA1withRSA");
			
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
			return pubKey;
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
