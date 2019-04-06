package pt.tecnico.sec;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
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
	private String[] users = {"user1, user2"};
	private char[] pwdArray = "password".toCharArray();
    private static PKI single_instance = null;

	
	
private PKI() {
	KEYSIZE = 1024 ;
	try {
		//get instance do keystore
		this.KEYSTORE = KeyStore.getInstance(KeyStore.getDefaultType());
		
		
		KEYSTORE.load(null, pwdArray);
		
		try(FileOutputStream fos = new FileOutputStream("newKeyStoreFileName.jks")) {
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

public Message setKey(String uID, PublicKey key) {
	
	KEYS.put(uID,key);
	return new Message(null, "OK", null,null, null);

}

public PublicKey getKey(String uID) throws Exception {
	System.out.println("key= "+ uID);
	System.out.println(KEYS.containsKey(uID));
	
	if(KEYS.containsKey(uID))
		return KEYS.get(uID);

	throw new Exception("No such user "+ uID + " "+ this.KEYS.keySet().toString());
}

public PrivateKey getMyKey(String id, String password) {//userID, password
	
    KeyStore.ProtectionParameter protParam =
            new KeyStore.PasswordProtection(password.toCharArray());
	
    KeyStore.PrivateKeyEntry pkEntry;
    PrivateKey myPrivateKey = null ;
	try {
		/*pkEntry = (KeyStore.PrivateKeyEntry)
		        KEYSTORE.getEntry(id , protParam);*/
		
				myPrivateKey = (PrivateKey) KEYSTORE.getKey(id, password.toCharArray());

	} catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
		e.printStackTrace();
	}
	System.out.println("handing out private key ");
        return myPrivateKey;
	
}

public KeyPair createKeys(String userID, String word) {
    KeyPairGenerator keyGen;
    KeyPair keyPair = null;
	try {
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(KEYSIZE);

		keyPair = keyGen.generateKeyPair();
					
		PublicKey pubKey = keyPair.getPublic();
		KEYS.put(userID,pubKey);
		
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
		return keyPair;
}


public byte[] encrypt(PrivateKey privateKey, String message) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");  
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);  

    return cipher.doFinal(message.getBytes());  
}


public byte[] decrypt(PublicKey publicKey, byte [] encrypted) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");  
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    
    return cipher.doFinal(encrypted);
}





X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm)
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
