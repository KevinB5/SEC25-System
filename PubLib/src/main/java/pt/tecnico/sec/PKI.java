package pt.tecnico.sec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

import sun.security.x509.*;
import java.security.cert.*;
import java.security.*;
import java.security.cert.*;





public class PKI {

	public static int KEYSIZE ;
	private static HashMap <String,PublicKey> KEYS = new HashMap <String,PublicKey>();// userID, Key
	public static KeyStore KEYSTORE;
	private static KeyPair notarykey; 
	private String[] users = {"user1, user2"};
	private static char[] pwdArray = "password".toCharArray();
    private static PKI single_instance = null;
    private static ProtectionParameter protParam = new KeyStore.PasswordProtection(pwdArray);
    private static String PATH;

	
	
private PKI() {
	KEYSIZE = 1024 ;
	try {
		//get instance do keystore
		this.KEYSTORE = KeyStore.getInstance("JKS");

		
		
		//KEYSTORE.load(null, pwdArray);
		Storage st = new Storage();
		PATH = Storage.originPath()+ "/KeyStoreFile.jks";

		File keystorefile = new File(PATH);
		InputStream keystoreStream = new FileInputStream(keystorefile);
		KEYSTORE.load(keystoreStream, pwdArray);
		
		//System.out.println(PATH);
		try(FileOutputStream fos = new FileOutputStream(PATH) ){
		    KEYSTORE.store(fos, pwdArray);
		}
		
	} catch (KeyStoreException e) {
		e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (CertificateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public static PKI getInstance() 
{ 
    if (single_instance == null) 
        single_instance = new PKI(); 

    return single_instance; 
} 

public static void setKey(String uID, X509Certificate cert) {
	
	//KEYS.put(uID,key);
	//System.out.println(KEYS.keySet().toString());
	try {
		File keystorefile = new File(PATH);
		InputStream keystoreStream = new FileInputStream(keystorefile);
		KEYSTORE.load(keystoreStream, pwdArray);

		KEYSTORE.setCertificateEntry(uID, cert);
		   FileOutputStream output = new FileOutputStream(PATH);
		    KEYSTORE.store(output, pwdArray);
		    output.close();
		    //System.out.println("xxx" +PATH);
	} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	//return new Message(null, "OK", null,null, null,null);

}

public static PublicKey getKey(String uID) throws Exception {
	
	KeyStore keyStore= KeyStore.getInstance("JKS");
	FileInputStream fos = new FileInputStream(PATH);


	keyStore.load(fos, pwdArray);


//System.out.println(KEYSTORE.containsAlias(uID));
//System.out.println(KEYSTORE.aliases());
	
	return keyStore.getCertificate(uID).getPublicKey();
	
}

public static PrivateKey getMyKey(String id) {//userID, password
	
	
    KeyStore.PrivateKeyEntry pkEntry;
    PrivateKey myPrivateKey = null ;
	try {
		/*pkEntry = (KeyStore.PrivateKeyEntry)
		        KEYSTORE.getEntry(id , protParam);*/
		
				myPrivateKey = (PrivateKey) KEYSTORE.getKey(id, pwdArray);

	} catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
		e.printStackTrace();
	}
	//System.out.println("handing out private key ");
        return myPrivateKey;
	
}


public static KeyPair getKeyPair( String userID) {
	  Key key = null;
	  PublicKey publicKey = null;
	try {
		key = (PrivateKey) KEYSTORE.getKey(userID, pwdArray);
		Certificate cert = KEYSTORE.getCertificate(userID);
		publicKey = cert.getPublicKey();
	} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
		e.printStackTrace();
	}


	  return new KeyPair(publicKey, (PrivateKey) key);
	}

public static KeyPair createKeys(String userID) {
    KeyPairGenerator keyGen;
    KeyPair keyPair = null;
	try {
		keyGen = KeyPairGenerator.getInstance("RSA");
		KEYSIZE=1024;
		keyGen.initialize(KEYSIZE);

		keyPair = keyGen.generateKeyPair();
					
		PublicKey pubKey = keyPair.getPublic();
		KEYS.put(userID,pubKey);
		
		System.out.println("saving Public key for "+ userID);
		
		
		 
		/*X509Certificate cert = generateCertificate(userID, getKeyPair(userID), 0, "SHA1withRSA");
		
		Certificate[] chain = {cert};
		
		  
		
		KeyStore.PrivateKeyEntry skEntry = new KeyStore.PrivateKeyEntry((PrivateKey) keyPair.getPrivate(), chain);

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
			e.printStackTrace();*/
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyPair;
}


public static byte[] encrypt(PrivateKey privateKey, String message) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");  
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);  

    return cipher.doFinal(message.getBytes());  
}


public byte[] decrypt(PublicKey publicKey, byte [] encrypted) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");  
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    
    return cipher.doFinal(encrypted);
}


public static boolean verifySignature(String data, byte[] signature, String uID) throws Exception {
	
	Signature sig = Signature.getInstance("SHA256withRSA");
	PKI.getInstance();
	sig.initVerify(PKI.getKey(uID));
	sig.update(data.getBytes());
	
	return sig.verify(signature);
}

static byte[] sign(String data, String idUser, String pass) throws InvalidKeyException, Exception{
	Signature rsa = Signature.getInstance("SHA256withRSA"); 
	rsa.initSign(getPrivateKey(idUser, pass));
	rsa.update(data.getBytes());
	//System.out.println("Signing " + data);
	return rsa.sign();
}

private static PrivateKey getPrivateKey(String idUser, String password) {//userID, password
	
    KeyStore.ProtectionParameter protParam =
            new KeyStore.PasswordProtection(password.toCharArray());
	
    KeyStore.PrivateKeyEntry pkEntry;
    PrivateKey myPrivateKey = null ;
	try {
		/*pkEntry = (KeyStore.PrivateKeyEntry)
		        KEYSTORE.getEntry(id , protParam);*/
		
				myPrivateKey = (PrivateKey) KEYSTORE.getKey(idUser, password.toCharArray());

	} catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
		e.printStackTrace();
	}
	System.out.println("handing out private key ");
        return myPrivateKey;
	
}


public static void createNotaryKeys(String notaryID) {
	notarykey = createKeys(notaryID);
	// TODO Auto-generated method stub
	
}

public static boolean containsKeyofUser(String id) {
	return KEYS.containsKey(id);
}   

}
