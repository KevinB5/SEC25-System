package pt.tecnico.sec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	public static int KEYSIZE=1024;
	private static HashMap <String,PublicKey> KEYS = new HashMap <String,PublicKey>();// userID, Key
	public static KeyStore KEYSTORE;
	private static char[] pwdArray = "password".toCharArray();
    private static PKI single_instance = null;
	private static String line = System.getProperty("file.separator");

//    private static ProtectionParameter protParam = new KeyStore.PasswordProtection(pwdArray);
    private static String PATH;

	
	
 private PKI() {
	try {
		//get instance do keystore
		KEYSTORE = KeyStore.getInstance("JKS");
		
		//KEYSTORE.load(null, pwdArray);
//		Storage st = new Storage();
		System.out.println(originPath()+line);
		PATH = originPath()+line+ "KeyStoreFile.jks";

		File keystorefile = new File(PATH);
		
			if(keystorefile.createNewFile()) {
				KEYSTORE.load(null, pwdArray);
			System.out.println("creating new ks");	
			}
			else {
				System.out.println("fetching ks");	

				InputStream keystoreStream = new FileInputStream(keystorefile);
				KEYSTORE.load(keystoreStream, pwdArray);
			}

		
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
public static String originPath() {
	String origin = System.getProperty("user.dir");
	int lastBar = 0;
	for(int i=0; i < origin.length() ; i++) {
		if(origin.charAt(i)==line.charAt(0))
			lastBar=i;
	}
	return origin.substring(0,lastBar);
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

		KEYSTORE.setCertificateEntry(uID+"c", cert);
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

public static PublicKey getPublicKey(String uID) throws Exception {
	
	File keystorefile = new File(PATH);
	InputStream keystoreStream = new FileInputStream(keystorefile);

	KEYSTORE.load(keystoreStream, pwdArray);


//System.out.println(KEYSTORE.containsAlias(uID));
//System.out.println(KEYSTORE.aliases());
	
	return KEYSTORE.getCertificate(uID+"c").getPublicKey();
	
}

public static PrivateKey getPrivateKey(String id, String pass) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {//userID, password
	
//  PrivateKey myPrivateKey = null ;
System.out.println("getting key for "+ id);
	
	KeyStore.ProtectionParameter protParam =    new KeyStore.PasswordProtection(pass.toCharArray());
	KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) KEYSTORE.getEntry(id, protParam);
	PrivateKey myPrivateKey = pkEntry.getPrivateKey();
//	
//    KeyStore.PrivateKeyEntry pkEntry;
//    PrivateKey myPrivateKey = null ;
//	try {
//		/*pkEntry = (KeyStore.PrivateKeyEntry)
//		        KEYSTORE.getEntry(id , protParam);*/
//				File keystorefile = new File(PATH);
//				InputStream keystoreStream = new FileInputStream(keystorefile);
//				KEYSTORE.load(keystoreStream,pwdArray);
//		
//				myPrivateKey = (PrivateKey) KEYSTORE.getKey(id, pass.toCharArray());
//
//	} catch (CertificateException|NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | IOException e) {
//		e.printStackTrace();
//	}
	//System.out.println("handing out private key ");*/
	
        return myPrivateKey;
	
}

/*
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
*/
/*
public static void createKeyPair(String userID, String password) throws NoSuchAlgorithmException, CertificateException {
    KeyPairGenerator keyGen;
    KeyPair keyPair = null;
	keyGen = KeyPairGenerator.getInstance("RSA");
	KEYSIZE=1024;
	keyGen.initialize(KEYSIZE);

	keyPair = keyGen.generateKeyPair();
	
	CertificateFactory cf = CertificateFactory.getInstance("X.509");
	Certificate cert = cf.generateCertificate(new InputStream(keyPair.getPublic().getEncoded()));
	PublicKey pubKey = keyPair.getPublic();
	KEYS.put(userID,pubKey);
	
	System.out.println("saving Public key for "+ userID);
		
		
		
}
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
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyPair;
}
*/

/*
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

*/

public static boolean verifySignature(String data, byte[] signature, String uID) throws Exception {
	
	Signature sig = Signature.getInstance("SHA256withRSA");
	PKI.getInstance();
	sig.initVerify(PKI.getPublicKey(uID));
	sig.update(data.getBytes());
	
	return sig.verify(signature);
}

public static boolean verifySignature(byte[] data, byte[] signature, String uID) throws Exception {
	
	Signature sig = Signature.getInstance("SHA256withRSA");
	PKI.getInstance();
	sig.initVerify(PKI.getPublicKey(uID));
	sig.update(data);
	
	return sig.verify(signature);
}

public static byte[] sign(String data, String idUser, String pass) throws InvalidKeyException, Exception{
//	System.out.println("Signing message");
	Signature rsa = Signature.getInstance("SHA256withRSA"); 
//	PrivateKey privatekey;
	
	KeyStore.ProtectionParameter protParam =    new KeyStore.PasswordProtection(pass.toCharArray());
	KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) KEYSTORE.getEntry(idUser, protParam);
	PrivateKey myPrivateKey = pkEntry.getPrivateKey();
//  privatekey = (PrivateKey) KEYSTORE.getKey(idUser, pass.toCharArray());
	rsa.initSign(myPrivateKey);
	rsa.update(data.getBytes());
	//System.out.println("Signing " + data);
//	System.out.println("Signed message");
	return rsa.sign();
}

public static byte[] sign(byte[] data, String idUser, String pass) throws InvalidKeyException, Exception{
//	System.out.println("Signing message");
	Signature rsa = Signature.getInstance("SHA256withRSA"); 
//	PrivateKey privatekey;
	
	KeyStore.ProtectionParameter protParam =    new KeyStore.PasswordProtection(pass.toCharArray());
	KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) KEYSTORE.getEntry(idUser, protParam);
	PrivateKey myPrivateKey = pkEntry.getPrivateKey();
//  privatekey = (PrivateKey) KEYSTORE.getKey(idUser, pass.toCharArray());
	rsa.initSign(myPrivateKey);
	rsa.update(data);
	//System.out.println("Signing " + data);
//	System.out.println("Signed message");
	return rsa.sign();
}

/*
private static PrivateKey getPrivateKey(String idUser, String password) {//userID, password
	
    KeyStore.ProtectionParameter protParam =
            new KeyStore.PasswordProtection(password.toCharArray());
	
    KeyStore.PrivateKeyEntry pkEntry;
    PrivateKey myPrivateKey = null ;
	try {
		/*pkEntry = (KeyStore.PrivateKeyEntry)
		        KEYSTORE.getEntry(id , protParam);
		
				myPrivateKey = (PrivateKey) KEYSTORE.getKey(idUser, password.toCharArray());

	} catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
		e.printStackTrace();
	}
	System.out.println("handing out private key ");
        return myPrivateKey;
	
}

/*
public static void createNotaryKeys(String notaryID, String password) {
	notarykey = createKeys(notaryID, password);
	// TODO Auto-generated method stub
	
}
*/

public static boolean containsKeyofUser(String id) {
	return KEYS.containsKey(id);
}   

//creates and stores keys in KeyStore
public static void createKeys(String userID, String pword) {
	try {
		if(getPublicKey(userID)==null) {
			KeyPairGenerator keyGen;
			KeyPair keyPair = null;
			char [] pwdArray = "password".toCharArray();
			X509Certificate cert = null;
			try {
				/*
				KEYSTORE = KeyStore.getInstance(KeyStore.getDefaultType());
				
				
				KEYSTORE.load(null, pwdArray);
				
				try(FileOutputStream fos = new FileOutputStream("newKeyStoreFileName.jks")) {
				    KEYSTORE.store(fos, pwdArray);
				}
				*/
				File keystorefile = new File(PATH);
				InputStream keystoreStream = new FileInputStream(keystorefile);
				KEYSTORE.load(keystoreStream, pwdArray);
				
				keyGen = KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(1024);
	
				keyPair = keyGen.generateKeyPair();
							
	//		pubKey = keyPair.getPublic();
				//KEYS.put(userID,pubKey);
				
	//		System.out.println("saving Public key for "+ userID);
				
	
				char[] password = pword.toCharArray();
				 
				cert = generateCertificate(userID,"Keys", keyPair, 7, "SHA256withRSA");
	
				
				//guardar o certificado na keystore para poder obter a pubkey
	
				setKey(userID, cert);
				
					
				Certificate[] chain = {cert};
				
				//guardar a privatekey
				KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(),chain);
	//        KEYSTORE.setEntry("privateKey",privateKeyEntry, new KeyStore.PasswordProtection(password));
				KEYSTORE.setKeyEntry(userID, keyPair.getPrivate(), password, chain);
				FileOutputStream output = new FileOutputStream(PATH);
	
				KEYSTORE.store(output, pwdArray);
	
				  /*
				System.out.println("Saving private key");
				KeyStore.PrivateKeyEntry skEntry = new KeyStore.PrivateKeyEntry((PrivateKey) keyPair.getPrivate(), chain);
				KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(password);
				
				KEYSTORE.setEntry(userID,skEntry,protParam);
				System.out.println("Private key safe and secure");
			//KEYSTORE.setKeyEntry(userID,(PrivateKey) keyPair.getPrivate(), password, null);
						*/
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
		}else
			System.out.println("já existe chave");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

private static X509Certificate generateCertificate(String dn, String subject, KeyPair pair, int days, String algorithm)
		  throws GeneralSecurityException, IOException
		{
		  PrivateKey privkey = pair.getPrivate();
		  X509CertInfo info = new X509CertInfo();
		  Date from = new Date();
		  Date to = new Date(from.getTime() + days * 86400000l);
		  CertificateValidity interval = new CertificateValidity(from, to);
		  BigInteger sn = new BigInteger(64, new SecureRandom());
		  X500Name owner = new X500Name("CN="+dn);
		  X500Name sj = new X500Name("CN="+subject);
		 
		  info.set(X509CertInfo.VALIDITY, interval);
		  info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
		  info.set(X509CertInfo.SUBJECT, sj);
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
